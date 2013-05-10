function [s,msg,lastdate,dts] = DTlatest(server,source,startdate,time_offset,maxdays,chan)
%Retrieves all data from a DataTurbine server after a specified starting date
%
%syntax: [s,msg,lastdate,dts] = DTlatest(server,source,startdate,time_offset,maxdays,chan)
%
%input:
%   server = DataTurbine server IP address (string; required)
%   source = DataTurbine source (string; required)
%   startdate = starting date for data retrieval (string date or numeric MATLAB serial date;
%      optional; default = [] for earliest date available in DataTurbine)
%   time_offset = offset from server in hours for both startdate and 'Date' field in s (numeric; optional;
%      default = 0 for no offset)
%   maxdays = maximum days of data to retrieve from Data Turbine in a single request - 
%      if maxdays is > 0 then data arrays will be retrieved in chunks and concatenated to limit
%      load on the server or network link (number; optional; default = [] or 0 for unlimited)
%   chan = channel to retrieve (string or cell array of strings for multiple channels; 
%      optional; default = '*' for all)
%
%output:
%   s = struct containing a 'Date' field (array of MATLAB serial dates) and fields
%       for each channel containing aligned data arrays
%   msg = text of any error message
%   lastdate = MATLAB serial date of the most recent observation (with time_offset applied)
%   dts = struct returned from the DTget function for analysis and debugging, containing server
%       information and raw channel data
%
%notes:
%   1) this function depends on DTstruct and DTget in the DTMatlabTK library; DTget requires that
%      rbnb.jar is registered in the permanent or runtime Java path, e.g. javaaddpath(which('rbnb.jar'))
%   2) specifying a non-zero time_offset will adjust both startdate and the aligned 'Date' field in [s] 
%      by time_offset
%   3) if startdate preceeds the earliest records available for source, all available data will be
%      returned without error (use datestr(s.Date(1)) to determine the actual earliest record date)
%   4) 1 extra day of data is requested from the server to account for discrepancies in the system
%      clocks of the local machine and Data Turbine server
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
%last modified: 08-Mar-2013

%init output
s = [];
lastdate = [];
dts = [];
msg = '';

%validate input
if nargin >= 2 && ~isempty(server) && ~isempty(source)
   
   %check for DTMatlabTK dependencies
   if exist('DTstruct','file') == 2 && exist('DTget','file') == 2
      
      %set default start date if omitted
      if exist('startdate','var') ~= 1
         startdate = [];
      end
      
      %set default time_offset if omitted or invalid
      if exist('time_offset','var') ~= 1 || isempty(time_offset) || ~isnumeric(time_offset)
         time_offset = 0;  %default to no offset if omitted or invalid
      end      
      
      %calculate starting date in seconds
      if isempty(startdate)
         dt = 0;
      else
         %convert starting date to seconds since 1970
         dt = date2sec(startdate,'1/1/1970',time_offset);
      end
      
      %check for maxdays option
      if exist('maxdays','var') ~= 1 || isempty(maxdays)
         maxdays = 0;
      end
      
      %use Java System currentTimeMillis method to get current UTC time in ms and convert to sec
      dt_current = java.lang.System.currentTimeMillis() ./ 1000;
      
      %check for valid date
      if ~isempty(dt)
         
         %supply default chan if omitted or invalid
         if exist('chan','var') ~= 1 || (~ischar(chan) && ~iscell(chan))
            chan = '*';
         end
         
         %init DT struct
         dts = DTstruct;
         
         %update fields for data retrieval
         dts.server = server;
         dts.source = source;
         
         %check for array of channels
         if ischar(chan)
            
            dts.chan = chan;
            
         else %cell array
            
            %init channel structure
            s_chan = DTchan;
            
            %add channel names to structure
            for n = 1:length(chan)
               s_chan(n).name = chan{n};
            end
            
            %store channel structure in dts
            dts.chan = s_chan;
            
         end
         
         %check for auto startdate, query DT server to get time of oldest record
         if dt == 0
            
            dts.start = 0;
            dts.reference = 'oldest';
            dts.duration = 0;
            
            try
               [dts,nchan] = DTget(dts);
            catch e
               dts = [];
               nchan = [];
               msg = parseDTgetError(e.message);
            end
            
            if ~isempty(nchan)
               dt = dts.start;
            else
               dt = [];
            end
            
         end
         
         %retrieve data based on start time
         if ~isempty(dt)

            %set time reference to absolute
            dts.reference = 'absolute';
            
            %check for maxdays option for retrieving data in chunks
            if maxdays > 0
               
               %calculate max seconds (86400 sec/day = 24 hr/day * 60 min/hr * 60 sec/min)
               maxsec = maxdays * 86400;
               
               %calculate number of increments for partial data retrieval
               num_inc = ceil((dt_current - dt) / maxsec);
               
               %init cell array for DTstructs from multiple queries
               dts_multi = cell(num_inc,1);
               
               %calculate array of starting dates
               %note: add 1 to num_inc to force num_inc start intervals
               if num_inc > 1
                  dt_inc = linspace(dt,dt_current,num_inc+1);
               else
                  dt_inc = dt;
               end

               %loop through increments requesting data
               for cnt = 1:num_inc
                  
                  %calculate dt offset to prevent data overlap
                  if cnt > 1
                     dt_offset = 0.0001;
                  else
                     dt_offset = 0;
                  end
                  dts.start = dt_inc(cnt) + dt_offset;
                  
                  %calculate duration
                  if cnt < num_inc
                     dts.duration = dt_inc(cnt+1) - dts.start;  %get next increment
                  else  %last increment
                     dts.duration = ceil(dt_current - dts.start + 86400); %get latest data, adding 1 day to make sure
                  end
                  
                  %get data, save to dts_multi
                  try
                     [dts_temp,nchan] = DTget(dts);
                     if nchan > 0
                        dts_multi{cnt} = dts_temp;
                     end
                  catch e                     
                     nchan = [];
                     msg = parseDTgetError(e.message);
                  end
                  
               end
               
               %combine channel data if necessary
               Ivalid = find(~cellfun('isempty',dts_multi));  %get index of non-empty structs
               if length(Ivalid) > 1
                  dts = DTcombine(dts_multi(Ivalid));  %combine channels in all structs
               else
                  dts = dts_multi{Ivalid(1)};  %return only non-empty struct
               end
               
            else  %all data at once
               
               %use absoluate time reference and calculated duration
               dts.start = dt;
               dts.duration = ceil(dt_current - dt + 86400); %get latest data, adding 1 day to make sure
               
               try
                  [dts,nchan] = DTget(dts);
               catch e
                  dts = [];
                  nchan = [];
                  msg = parseDTgetError(e.message);
               end
               
            end
            
         else
            nchan = [];
         end
         
         %convert DTstruct to standard data structure if valid channel data returned
         if ~isempty(nchan) && nchan > 0

            %call DTalign to convert DTstruct to standard struct with channels as fields plus datetime
            [s,msg] = DTalign(dts,time_offset);
            
            %retrieve maximum date from data structure
            if ~isempty(s) && isfield(s,'Date');
               lastdate = s.Date(end);
            end
            
         elseif isempty(msg)
            %generate null data reference if no system error messages were generated
            msg = 'no data were returned from DTget - confirm source name and date range';
         end
         
      else
         msg = 'invalid starting date';
      end
      
   else
      msg = 'required functions in the DTMatlabTK library were not found in the MATLAB path';
   end
   
else
   msg = 'missing input: a valid server and source are required';
end
return


function msg = parseDTgetError(err)
%Parses system errors returned from running DTget to generate a friendly error message
%
%syntax: msg = parseDTgetError(err)
%
%input:
%   err = system error message
%
%output:
%   msg = friendly error message
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
%last modified: 15-Apr-2013

if ~isempty(strfind(err,'com.rbnb.sapi.Client.OpenRBNBConnection'))
   msg = 'an error occurred connecting to the Data Turbine Server';
elseif ~isempty(strfind(err,'invalid input structure'))
   msg = 'Invalid Data Turbine source';   
else
   msg = ['Error running DTget: ',err];
end

return