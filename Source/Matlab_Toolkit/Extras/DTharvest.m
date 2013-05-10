function [msg,status] = DTharvest(operation,server,source,options)
%Manages automatic retrieval and archiving of channel data from a Data Turbine source
%
%syntax: [msg,status] = DTharvest(operation,server,source,options)
%
%input:
%   operation = operation to perform (string; default = 'list'):
%      'start' = initialize or restart a timer for collecting data on a schedule
%      'stop' = pause data collection
%      'resume' = resume data collection from last logged harvest
%      'delete' = stop data collection and clear the timer from memory
%      'list' = list the status of all operating Data Turbine harvesters
%      'harvest' = harvest new data observations from the Data Turbine source (called by the timer)
%      'viewlog' = returns all log messages for the specified server and source as a character array
%      'options' = returns the options structure for the specified server and source
%   server = DataTurbine server IP address (string; required unless option = 'list')
%   source = DataTurbine source (string; required unless option = 'list')
%   options = structure containing harvest configuration options - see DTharvestStruct.m
%      (struct; required if operation = 'start' and harvest timer not already initialized)
%
%output:
%   msg = text of any error or warning message
%   status = status flag (1 = success, 0 = failure)
%
%usage notes:
%   1) this function will generate or update a log file 'DTharvestLog.mat' in the DTharvest.m directory
%      when first run; this file can be moved to another directory in the MATLAB path if desired
%   2) options will be ignored unless operation = 'start' or 'resume'
%   3) server, source and options will be ignored if operation = 'list'
%   4) if operation = 'viewlog' or 'options' and server and source do not match any running timers,
%      cached logs and options will be retrieved from DTharvestLog.mat if present
%   5) see 'help DTharvestStruct' for information on defining post-harvest workflows and
%      other advanced options
%
%example code:
%
%  %DTharvest.m example using the built-in _Metrics channel of Data Turbine, with a simple plot workflow
%  %(note: substitute a remote server address for 'localhost' if not running Data Turbine locally)
%
%  %get initial options struct for a 1-minute harvest with data saved to 'dt_metrics.mat', with no time offset
%  options = DTharvestStruct('dt_metrics.mat',1/60,0);
%
%  %add workflow step to plot MemoryUsed at the time of harvest (help DTharvestStruct for information)
%  options.Workflow = 'plot(s_aligned.Date,s_aligned.MemoryUsed,''bd-''); datetick(''x'',''HH:MM:SS'')';
%
%  %start harvests
%  DTharvest('start','localhost','_Metrics',options)
%
%  %wait for several minutes, watching plots update after each harvest
%  pause(180)
%
%  %view cumulative harvest logs
%  DTharvest('viewlog','localhost','_Metrics') 
%
%  %stop harvests temporarily
%  DTharvest('stop','localhost','_Metrics')
%
%  %revise options to include a workflow for plotting all harvested data (not just the latest)
%  %(note that [FileAligned] and [VariableAligned] tokens are resolved to fields in options when run)
%  options.Workflow = ['vars=load(''[FileAligned]'',''[VariableAligned]'');' ...
%     'plot(vars.[VariableAligned].Date,vars.[VariableAligned].MemoryUsed,''bd-'');' ...
%     'datetick(''x'',''HH:MM:SS'')'];
%
%  %resume harvests with the revised options
%  DTharvest('resume','localhost','_Metrics',options)
%
%  %wait for several minutes, watching plots update after each harvest
%  pause(180)
%
%  %cancel harvests
%  DTharvest('delete','localhost','_Metrics')
%
%
%author:
%  Wade Sheldon
%  Department of Marine Sciences
%  University of Georgia
%  Athens, GA 30602-3636
%  sheldon@uga.edu
%
%Copyright 2013 Open Source Data Turbine Initiative
%
%Licensed under the Apache License, Version 2.0 (the "License");
%you may not use this file except in compliance with the License.
%You may obtain a copy of the License at
%
%    http://www.apache.org/licenses/LICENSE-2.0
%
%Unless required by applicable law or agreed to in writing, software
%distributed under the License is distributed on an "AS IS" BASIS,
%WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
%See the License for the specific language governing permissions and
%limitations under the License.
%
%last modified: 08-May-2013

%init output
msg = '';
status = 0;

%check for no arguments
if nargin == 0
   operation = 'list';
end

%validate input
if ~isempty(operation)
   
   %supply defaults for omitted arguments
   if exist('server','var') ~= 1
      server = '';
   end
   
   if exist('source','var') ~= 1
      source = '';
   end
   
   if exist('options','var') ~= 1
      options = [];
   end
   
   %run requested operation
   switch operation
      
      case 'start'  %initialize or restart the harvest
         
         %check for existing timer object
         t = getTimerHandle(server,source);

         if ~isempty(t)
            
            %check for options argument
            if ~isempty(options)
               
               %get lastdate and log from existing timer to update options
               options_old = get(t,'UserData');
               if ~isempty(options_old)
                  options.LastDate = options_old.LastDate;
                  options.Log = options_old.Log;
               else
                  options.Log = [];
               end
               
               %stop and delete timer
               stop(t);
               delete(t);
               
               %start new harvester
               [msg,status] = DTharvest('start',server,source,options);
               
            else  %restart current timer with existing options
               
               %check running state
               timer_state = get(t(1),'Running');
               
               %start timer if off
               if strcmp(timer_state,'off')
                  
                  %get start time from options
                  options = get(t,'UserData');
                  starttime = options.StartTime;
                  
                  %check for specific start time
                  if ~isempty(starttime)
                     
                     %call function to determine next start time
                     period_sec = options.Period .* 3600;
                     dstr = getStartDate(starttime,period_sec);
                     
                     %start with delay
                     try
                        startat(t,dstr);
                        status = 1;
                        msg = ['re-started harvester for ',server,'/',source,' - next harvest at ',dstr];
                     catch e
                        status = 0;
                        msg = ['error starting havester for ',server,'/',source,' - ',e.message];
                     end
                     
                  else  %start immediately
                     
                     try
                        start(t);
                        status = 1;
                        msg = ['re-started harvester for ',server,'/',source,' - next harvest at ',datestr(now)];
                     catch e
                        status = 0;
                        msg = ['error starting havester for ',server,'/',source,' - ',e.message];
                     end
                     
                  end
                  
               else
                  
                  status = 0;
                  msg = ['harvester for ',server,'/',source,' is already running'];
                  
               end
               
            end
               
         else  %create new timer
            
            if isstruct(options) && isfield(options,'FileRaw')
               
               %check for valid period and at least 1 save file
               if options.Period > 0 && (~isempty(options.FileRaw) || ~isempty(options.FileAligned))
                  
                  %generate tagname, timer name and timer function from server, source
                  tagname = ['DT_',server,'/',source];
                  timername = ['Data Turbine Harvester (',server,'/',source,')'];
                  timerfcn = ['DTharvest(''harvest'',''',server,''',''',source,''');'];
                  errfcn = ['DTharvest(''error'',''',server,''',''',source,''');'];
                  
                  %calculate period in seconds
                  period_sec = options.Period .* 3600;
                  
                  %initialize timer
                  t = timer;
                  set(t, ...
                     'BusyMode','queue', ...
                     'ErrorFcn',errfcn, ...
                     'ExecutionMode','fixedRate', ...
                     'Name',timername, ...
                     'ObjectVisibility','on', ...
                     'Period',period_sec, ...
                     'StartDelay',0, ...
                     'Tag',tagname, ...
                     'TimerFcn',timerfcn, ...
                     'UserData',options ...
                     );
                  
                  %get start time from options
                  starttime = options.StartTime;
                  
                  %check for specific start time
                  if ~isempty(starttime)
                     
                     %call function to determine next start time
                     dstr = getStartDate(starttime,period_sec);
                     
                     try
                        startat(t,dstr);
                        status = 1;
                        msg = ['started harvester for ',server,'/',source,' - next harvest at ',dstr];
                     catch e
                        status = 0;
                        msg = ['error starting harvester for ',server,'/',source,' - ',e.message];
                     end
                     
                  else  %start immediately
                     
                     try
                        start(t);
                        status = 1;
                        msg = ['started harvester for ',server,'/',source,' - next harvest at ',datestr(now)];
                     catch e
                        status = 0;
                        msg = ['error starting harvester for ',server,'/',source,' - ',e.message];
                     end
                     
                  end
                  
               else
                  status = 0;
                  msg = 'timers must have a Period > 0 and at least 1 output file defined';
               end
               
            else
               status = 0;
               msg = 'invalid options - see DTharvestStruct.m';
            end
            
         end
         
      case 'resume'  %resume harvests based on saved configuration options in DTharvestLog.mat

         %get filename of harvest log
         fn = which('DTharvestLog.mat');
         harvest_log = [];
         
         %check for log file
         if ~isempty(fn)
            
            %load log file
            try
               vars = load(fn);
            catch e
               vars = struct('null','');
               msg = e.message;
            end
            
            %check for log variable
            if isfield(vars,'harvest_log')
               
               %get log variable
               harvest_log = vars.harvest_log;
               
               %look up entry for server:source
               idx = find(strcmpi(server,{harvest_log.server}) & strcmpi(source,{harvest_log.source}));
               
               %call 'start' method with cached options struct
               if ~isempty(idx)
                  
                  %get cached options
                  options_new = harvest_log(idx).options;
                  
                  %check for new options argument
                  if isempty(options)
                     options = options_new;  %use cached options
                  else
                      %retain log entries and LastDate from cached options
                     options.Log = options_new.Log;
                     options.LastDate = options_new.LastDate;
                  end
                  
                  %restart harvester
                  [msg,status] = DTharvest('start',server,source,options);
                  
               else
                  msg = ['no logs for ',server,' source ',source,' were found'];
               end
               
            else
               if isempty(msg)
                  msg = 'harvest_log variable was not present in DTharvestLog.mat';
               end
            end
            
         else
            msg = 'log file DTharvestLog.mat was not found in the MATLAB path';
         end
         
         %set status flag
         if isempty(harvest_log)
            status = 0;
         end        
         
      case 'error'  %handle timer execution errors
         
         %get timer handle and options
         [t,options] = getTimerHandle(server,source);
         
         %generate log entry
         if isstruct(options)
            
            %get logging option
            logopt = options.LogOption;
            starttime = options.StartTime;
            period = options.Period;
            
            %calculate next interval
            if ~isempty(starttime)
               period_sec = options.Period * 3600;
               starttime = getStartDate(starttime,period_sec);
            else
               starttime = datestr(now + period/24);
            end
            
            %calculate next start time
            try
               startat(t,starttime);
               str_restart = ['Next attempt scheduled at ',starttime];
            catch e
               str_restart = ['Attempt to restart the harvester failed with error: ',e.message];
            end
            
            %check for a file or email log option
            if ~isempty(logopt)
               
               %generate error message
               str = ['An error occurred harvesting channel data from ''',source,'''', ...
                  'on the Data Turbine server ',server,' at ',datestr(now),' - ',str_restart];
               
               %check for file option
               if strfind(logopt,'file')
                  fn = options.FileRaw;
               else
                  fn = '';  %do not save error entry to logfile
               end
               
               %check for email option
               if strfind(logopt,'email')
                  email = options.Email;
                  subject = ['Data Turbine harvester error for ',server,'/',source];
               else
                  email = '';  %do not email error entry
                  subject = '';
               end
               
               %send error log entry to file and/or email
               [msg,status] = logOperations(str,fn,email,subject);
               
            else
               status = 0;
               msg = 'no logging option specified';
            end
            
         else
            status = 0;
            msg = 'invalid harvester options';
         end
         
      case 'list'  %list status of all Data Turbine harvesters in memory
         
         %get handles of all timer objects
         h = timerfindall;
         num = length(h);
         
         if num > 0
            
            %init states
            states = repmat({''},num,1);
            
            %loop through timers getting status info
            for n = 1:num
               
               %get timer handle from array
               t = h(n);
               
               %check for DT timer based on tag
               tagname = get(t,'Tag');
               
               %get info
               if strncmp(tagname,'DT_',3)
                  t_name = get(t,'Name');
                  t_period = get(t,'Period');
                  t_running = get(t,'Running');
                  t_tasks = get(t,'TasksExecuted');
                  states{n} = [t_name,': running = ',t_running, ...
                     ', period = ',num2str(t_period),' sec', ...
                     ', tasks = ',int2str(t_tasks)];
               end

               %get index of valid Data Turbine timers
               Ivalid = find(~cellfun('isempty',states));
               
               %generate output message
               if ~isempty(Ivalid)
                  status = 1;
                  msg = char(states(Ivalid));
               else
                  status = 0;
                  msg = 'no Data Turbine harvest timers were found';
               end
               
            end
            
         else
            status = 0;
            msg = 'no harvest timers were found in memory';
         end
         
      case 'delete'  %stop harvests and remove the timer from memory
         
         %get active timer handle
         t = getTimerHandle(server,source);
         
         if ~isempty(t)
            stop(t);
            delete(t)
            status = 1;
            msg = ['stopped harvests for ',server,'/',source,' and removed timer from memory'];
         else
            status = 0;
            msg = ['no active harvester was found for ',server,'/',source];
         end
         
      case 'stop'  %stop harvests temporarily
         
         %get active timer handle
         t = getTimerHandle(server,source);
         
         if ~isempty(t)
            stop(t);
            status = 1;
            msg = ['paused harvests for ',server,'/',source,' - use ''start'' or ''resume'' to restart harvests'];
         else
            status = 0;
            msg = ['no active harvester was found for ',server,'/',source];
         end
         
      case 'viewlog'  %display harvest log
         
         if ~isempty(server) && ~isempty(source)
            
            %check for active harvester
            t = getTimerHandle(server,source);
            
            %init log variable
            log = [];
            
            %get log from running timer or log file
            if ~isempty(t)
               
               options = get(t,'UserData');
               log = options.Log;
               
               %add linefeeds for parity with format in DTharvestLog.mat
               for n = 1:length(log)
                  log(n).entry = strrep(log(n).entry,'; ',char(10));
               end
               
            else  %load cached log
               
               %get filename of harvest log
               fn = which('DTharvestLog.mat');
               
               %check for valid log file
               if ~isempty(fn)
                  try
                     vars = load(fn,'-mat');
                  catch e
                     vars = struct('null','');
                     msg = ['error loading log file (',e.message];
                  end
                  if isfield(vars,'harvest_log') && isfield(vars.harvest_log,'server') && isfield(vars.harvest_log,'source')
                     Imatch = find(strcmp(server,{vars.harvest_log.server}) & strcmp(source,{vars.harvest_log.source}));
                     if ~isempty(Imatch)
                        log = vars.harvest_log(Imatch(1)).log;
                     end
                  else
                     msg = 'no valid harvest log found';
                  end
               end
               
            end
            
            %check for valid log structure
            if ~isempty(log)
               %generate character array of log messages
               msg = [char({log.date}'), ...
                  repmat(': ',length(log),1), ...
                  char(strrep({log.entry}',char(10),[char(10),blanks(22)]))];
            elseif isempty(msg)
               msg = 'no valid harvest log found';
            end
            
         else
            msg = 'invalid server and source';
         end
        
      case 'options'  %return options structure of active timer or from cached log entry

         if ~isempty(server) && ~isempty(source)
            
            %get options structure from active timer userdata
            [~,options] = getTimerHandle(server,source);
            
            %check for active timer
            if isempty(options) || ~isstruct(options)
               
               %get filename of harvest log
               fn = which('DTharvestLog.mat');
               
               %check for valid log file
               if exist(fn,'file') == 2
                  try
                     vars = load(fn,'-mat');
                  catch e
                     vars = struct('null','');
                     msg = ['error loading log file (',e.message];
                  end
                  if isfield(vars,'harvest_log') && isfield(vars.harvest_log,'server') && isfield(vars.harvest_log,'source')
                     Imatch = find(strcmp(server,{vars.harvest_log.server}) & strcmp(source,{vars.harvest_log.source}));
                     if ~isempty(Imatch)
                        options = vars.harvest_log(Imatch(1)).options;
                     else
                        msg = ['no options for Data Turbine ',server,'/',source,' were present in DTharvestLog.mat'];
                     end
                  else
                     msg = 'harvest log file DTharvestLog.mat is invalid';
                  end
               else
                  msg = 'harvest log file DTharvestLog.mat does not exist';
               end
               
            end
            
            %check for options - return to base workspace
            if ~isempty(options) && isstruct(options)
               assignin('base','options',options)
               msg = 'options structure returned to base workspace as the variable ''options''';
            end
            
         else
            msg = 'invalid server and source';
         end
         
      case 'harvest'  %harvest new data and generate output files
         
         %get active time handle and options
         [t,options] = getTimerHandle(server,source);
         
         %check for valid timer handle and options
         if ~isempty(t) && isstruct(options)
            
            %get harvest options
            time_offset = options.TimeOffset;
            fn_raw = options.FileRaw;
            var_raw = options.VariableRaw;
            fn_aligned = options.FileAligned;
            var_aligned = options.VariableAligned;
            lastdate = options.LastDate;
            
            %resolve date/time tokens in filenames
            fn_raw = fillDateTokens(fn_raw);
            fn_aligned = fillDateTokens(fn_aligned);
            
            %get logging options
            logopt = options.LogOption;
            if ~isempty(strfind(logopt,'email'))
               email = options.Email;
            else
               email = '';
            end
            
            %add 1 ms to date to prevent overlap from last request
            if ~isempty(lastdate)
               startdate = lastdate + 0.001/86400;
            else
               startdate = [];
            end
            
            %harvest data from server
            [s_latest,msg,lastdate,s_raw] = DTlatest(server,source,startdate,time_offset);
            
            if ~isempty(s_latest)
               
               %save raw file
               if ~isempty(fn_raw) && ~isempty(s_raw)
                  [s_all,msg_raw] = DTappend(fn_raw,s_raw,var_raw,1);
               else
                  s_all = [];
                  msg_raw = '';
               end
               
               %save aligned struct
               if ~isempty(fn_aligned) && ~isempty(s_raw)
                  if isempty(s_all)
                     %append new data to existing without saving if not already done for raw file
                     %to get aligned struct for entire time period
                     [s_all,msg_raw] = DTappend(fn_raw,s_raw,var_raw,0);
                  end
                  if ~isempty(s_all)
                     [s_aligned,msg_aligned] = DTalign(s_all,time_offset);
                     if ~isempty(s_aligned)
                        outfile = struct(var_aligned,s_aligned);    %#ok<NASGU> - suppress spurious mlint
                        if exist(fn_aligned,'file') == 2
                           save(fn_aligned,'-struct','outfile','-append')
                        else
                           save(fn_aligned,'-struct','outfile')
                        end
                     end
                  else
                     s_aligned = [];
                     msg_aligned = '';
                  end
               else
                  s_aligned = [];
                  msg_aligned = '';
               end
               
            end
            
            %check for successful harvest
            if ~isempty(s_latest)
               
               %calculate number of observatsion
               numobs = length(s_latest.Date);
               
               %update lastdate in cached options
               options.LastDate = lastdate;
               
               %init log entry
               str_log = ['Retrieved ',int2str(numobs),' observations for ' ...
                  int2str(length(fieldnames(s_latest))-1),' channels from ''',source, ...
                  ''' on Data Turbine server ',server,' at ',datestr(now)];
               
               status = 1;
               
            else
               
               %init log entry
               str_log = ['Failed to retrieve data from ''',source, ...
                  ''' on Data Turbine server ',server,' at ',datestr(now),': ',msg];
               
               status = 0;
               numobs = 0;
               
            end
            
            %generate raw file saving entry
            if ~isempty(s_latest) && ~isempty(fn_raw) && ~isempty(var_raw)
               if ~isempty(s_all)
                  str_raw = ['Successfully appended channel data to ',var_raw,' in ',fn_raw];
               else
                  str_raw = ['Errors occurred appending channel data to ',var_raw,' in ',fn_raw,': ',msg_raw];
               end
            else
               str_raw = 'Raw channel data not saved';
            end
            
            %generate aligned struct file saving entry
            if ~isempty(s_latest) && ~isempty(fn_aligned) && ~isempty(var_aligned)
               if ~isempty(s_aligned)
                  str_aligned = ['Successfully saved aligned channel data as ',var_aligned,' in ',fn_aligned];
               else
                  str_aligned = ['Errors occurred saving aligned channel data as ',var_aligned,' in ',fn_aligned,': ',msg_aligned];
               end
            else
               str_aligned = 'Aligned channel data not saved';
            end
            
            %check for workflow
            workflow = options.Workflow;
            
            if ~isempty(s_latest) && ~isempty(workflow)
                              
               %build array of tokens to replace in workflow statements
               tokens = {'[FileRaw]',options.FileRaw ; ...
                  '[VariableRaw]',options.VariableRaw ; ...
                  '[FileAligned]',options.FileAligned ; ...
                  '[VariableAligned]',options.VariableAligned};
               
               %replace tokens
               for cnt = 1:size(tokens,1)
                  workflow = strrep(workflow,tokens{cnt,1},tokens{cnt,2});
               end
               
               %call subfunction to run workflow in dedicated workspace
               [wf_msg,wf_status] = runWorkflow(workflow,s_latest,s_raw);
               
               if wf_status == 1
                  str_workflow = 'Successfully executed post-harvest workflow';
               else
                  str_workflow = wf_msg;
               end  
               
            else
               str_workflow = 'No post-harvest workflow run';
            end
            
            %concatenate log entrys to create message
            msg = [str_log,'; ',str_raw,'; ',str_aligned,'; ',str_workflow];
            
            %add log entry to options
            harvest_log = options.Log;
            if isempty(harvest_log)
               idx = 1;
            else
               idx = length(harvest_log)+1;
            end
            harvest_log(idx).date = datestr(now);
            harvest_log(idx).observations = numobs;
            harvest_log(idx).entry = msg;
            
            %update cached options to include LastDate and Log changes
            options.Log = harvest_log;
            set(t,'UserData',options)
            
            %save or email log if specifed
            if ~isempty(logopt)
               
               %get filename for master log file
               fn_log = which('DTharvestLog.mat');
               if isempty(fn_log)
                  pn = fileparts(which('DTharvest.m'));
                  fn_log = [pn,filesep,'DTharvestLog.mat'];
               end
               
               %format log entries as multi-line string with carriage returns
               lf = char(10);
               str = [str_log,lf,str_raw,lf,str_aligned,lf,str_workflow];
               
               %generate email subject line
               subject = ['Harvest report for Data Turbine ',server,'/',source];
               
               %call log function
               msg_log = logOperations(server,source,options,numobs,str,fn_log,email,subject);
               
            else
               msg_log = '';
            end
            
            %check for console update option
            console = options.Console;
            if strcmp(console,'all') || (strcmp(console,'error') && status == 0)
               disp(strrep(msg,'; ',[char(10),'   ']))
               if ~isempty(msg_log)
                  disp(msg_log)  %display logging errors
               end
            end
            
         else
            status = 0;
            msg = 'invalid harvester options';
         end
         
      otherwise
         
         status = 0;
         msg = 'unsupported operation';
         
   end
   
else
   
   status = 0;
   msg = 'invalid input';
   
end

return


function [t,options] = getTimerHandle(server,source)
%Retrieves a timer object for a specified Data Turbine server and source
%
%syntax: [t,options] = getTimerHandle(server,source)
%
%input:
%  server = Data Turbine server IP  (string; required)
%  source = Data Trubine server source (string; required)
%
%output:
%  t = timer object handle
%  options = options structure cached as userdata in the timer object
%
%author:
%  Wade Sheldon
%  Department of Marine Sciences
%  University of Georgia
%  Athens, GA 30602-3636
%  sheldon@uga.edu
%
%Copyright 2013 Open Source Data Turbine Initiative
%
%Licensed under the Apache License, Version 2.0 (the "License");
%you may not use this file except in compliance with the License.
%You may obtain a copy of the License at
%
%    http://www.apache.org/licenses/LICENSE-2.0
%
%Unless required by applicable law or agreed to in writing, software
%distributed under the License is distributed on an "AS IS" BASIS,
%WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
%See the License for the specific language governing permissions and
%limitations under the License.
%
%last modified: 05-Apr-2013

t = [];
options = [];

if nargin == 2 && ~isempty(server) && ~isempty(source)
   
   %generate tagname based on server and source
   tagname = ['DT_',server,'/',source];
   
   %search for timer object with matching tag
   t = timerfindall('Tag',tagname);
   
   %get userdata
   if ~isempty(t)
      options = get(t(1),'UserData');
   end
   
end

return


function dstr = getStartDate(starttime,period_sec)
%Calculate start date based on a starting time and the current time
%
%syntax: dstr = getStartDate(starttime)
%
%input:
%   starttime = string containing time to start (e.g. '01:30')
%   period_sec = harvest period in seconds
%
%output:
%   dstr = date string containing next time to start
%
%author:
%  Wade Sheldon
%  Department of Marine Sciences
%  University of Georgia
%  Athens, GA 30602-3636
%  sheldon@uga.edu
%
%Copyright 2013 Open Source Data Turbine Initiative
%
%Licensed under the Apache License, Version 2.0 (the "License");
%you may not use this file except in compliance with the License.
%You may obtain a copy of the License at
%
%    http://www.apache.org/licenses/LICENSE-2.0
%
%Unless required by applicable law or agreed to in writing, software
%distributed under the License is distributed on an "AS IS" BASIS,
%WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
%See the License for the specific language governing permissions and
%limitations under the License.
%
%last modified: 04-Apr-2013

%check input
if nargin == 2
   
   %get current day
   dy = fix(now);
   
   %calculate first runtime from beginning of day
   try
      dt_next = dy + datenum(['1/0/0000 ',starttime]);  %calculate initial starting date
   catch                                                                                  %#ok<CTCH>
      dt_next = dy;
   end
   
   %check for past/future date and adjust start if necessary
   if dt_next > dt  %before start time
      dstr = datestr(dt_next,0);  %convert to string
   else  %after start time, kick to period after current time
      while dt > dt_next
         dt_next = dt_next + (period_sec ./ 86400);  %increment by Period converted to fractional days
      end
      dstr = datestr(dt_next,0);  %convert adjusted date to string
   end
   
else
   dstr = datestr(now,0);  %missing input - return current date
end

return


function [msg,status] = logOperations(server,source,options,numobs,str,fn,email,subject)
%Saves log entries from a harvest to disk and/or sends email reports
%
%syntax: [msg,status] = logOperations(server,source,options,numobs,str,fn,email,subject)
%
%input:
%  server = Data Turbine server
%  source = Data Turbine source
%  options = harvest options structure
%  numobs = number of data rows harvested
%  str = log entry string
%  fn = filename for saving log ('' if none)
%  email = email address for sending log ('' if none)
%  subject = subject line for email
%
%output:
%  msg = text of any error message
%  status = status flag (1 = success, 0 = failure)
%
%author:
%  Wade Sheldon
%  Department of Marine Sciences
%  University of Georgia
%  Athens, GA 30602-3636
%  sheldon@uga.edu
%
%Copyright 2013 Open Source Data Turbine Initiative
%
%Licensed under the Apache License, Version 2.0 (the "License");
%you may not use this file except in compliance with the License.
%You may obtain a copy of the License at
%
%    http://www.apache.org/licenses/LICENSE-2.0
%
%Unless required by applicable law or agreed to in writing, software
%distributed under the License is distributed on an "AS IS" BASIS,
%WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
%See the License for the specific language governing permissions and
%limitations under the License.
%
%last modified: 07-May-2013

status = 0;
msg = '';

if nargin == 8 && ~isempty(server) && ~isempty(source) && ~isempty(options) && ~isempty(str)
   
   if ~isempty(fn)
      
      %load existing log variable from file
      if exist(fn,'file') == 2
         try
            vars = load(fn,'-mat');
         catch e
            vars = struct('null','');
            msg = ['an error occurred loading existing log file: ',e.message];
         end
      else
         vars = struct('null','');
      end
      
      %add entry to existing log struct or create if not present
      if isfield(vars,'harvest_log')
         
         %check for existing logs
         harvest_log = vars.harvest_log;
         idx = find(strcmpi(server,{harvest_log.server}) & strcmpi(source,{harvest_log.source}));
         
         %add new entry
         if isempty(idx)
            idx = length(harvest_log)+1;
            harvest_log(idx).server = server;
            harvest_log(idx).source = source;
            harvest_log(idx).options = options;
            harvest_log(idx).log = struct('date',datestr(now),'observations',numobs,'entry',str);    %#ok<NASGU> - suppress spurious mlint
         else
            harvest_log(idx).options = options;
            idx2 = length(harvest_log(idx).log) + 1;
            harvest_log(idx).log(idx2).date = datestr(now);
            harvest_log(idx).log(idx2).observations = numobs;
            harvest_log(idx).log(idx2).entry = str;                                                  %#ok<NASGU> - suppress spurious mlint
         end
         
      else  %create new struct
         
         %create log struct
         harvest_log = struct( ...
            'server',server, ...
            'source',source, ...
            'options',options, ...
            'log',struct('date',datestr(now),'observations',numobs,'entry',str));            %#ok<NASGU> - suppress spurious mlint
      
      end

      %save log to file
      try
         if exist(fn,'file') == 2
            save(fn,'harvest_log','-append')
         else
            save(fn,'harvest_log')
         end
         status = 1;
      catch e
         status = 0;
         msg = ['Log file save error: ',e.message];
      end
      
   end
   
   %check for email option
   if ~isempty(email)
      
      %add extra whitespace to log entries for emailing
      email_body = strrep(str,char(10),[char(10) char(10)]);
      
      %try to send mail
      try
         sendmail(email,subject,email_body);
         status = 1;
      catch e
         status = 0;
         msg = ['Log email error: ',e.message];
      end
      
   end
   
end

return


function [msg,status] = runWorkflow(workflow,s_aligned,s_raw)
%Executes a workflow expression
%
%syntax: [msg,status] = runWorkflow(workflow,s_aligned,s_raw)
%
%input:
%  workflow = string containing MATLAB script to execute using 'eval'
%  s_aligned = aligned channel structure from DTlatest.m
%  s_raw = DTstruct containing raw channel data from DTlatest.m
%
%output:
%  msg = text of any error message
%  status = status flag (1 = success, 0 = error)
%
%author:
%  Wade Sheldon
%  Department of Marine Sciences
%  University of Georgia
%  Athens, GA 30602-3636
%  sheldon@uga.edu
%
%Copyright 2013 Open Source Data Turbine Initiative
%
%Licensed under the Apache License, Version 2.0 (the "License");
%you may not use this file except in compliance with the License.
%You may obtain a copy of the License at
%
%    http://www.apache.org/licenses/LICENSE-2.0
%
%Unless required by applicable law or agreed to in writing, software
%distributed under the License is distributed on an "AS IS" BASIS,
%WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
%See the License for the specific language governing permissions and
%limitations under the License.
%
%last modified: 07-May-2013

status = 0;
msg = '';

if nargin == 3 && ~isempty(workflow) && isstruct(s_aligned) && isstruct(s_raw)

   try
      eval(workflow)
      status = 1;
   catch e
      msg = ['An error occurred running the workflow: ',e.message];
   end
   
else
   msg = 'Invalid workflow string';   
end

return
