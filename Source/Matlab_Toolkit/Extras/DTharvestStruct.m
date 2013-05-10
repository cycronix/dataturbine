function options = DTharvestStruct(fn,period,time_offset)
%Generates an options structure for use with DTharvest.m containing default settings for fields
%
%syntax: options = DTharvestStruct(fn,period,time_offset)
%
%input:
%   fn = fn = fully-qualified filename to use for saving harvested channel data (string; optional; default = '')
%   period = data collection frequency in hours (number; optional; default = 1)
%   time_offset = time offset from server in hours (number; optional; default = 0)
%
%output:
%   options = harvest timer configuration options - see DTharvest.m (struct):
%      'Period' = data collection frequency in hours (number)
%      'StartTime' = time of day to start harvests; used in conjunction with Period to determine
%         exact start time of the first harvest, e.g. 
%         Period = 1 and StartTime = 09:45 will start hourly harvests at 09:45 or the next interval
%         Period = 24 and StartTime = 05:00 will start daily harvests at 05:00 or the next interval
%         (string; hh:mm; default = '' to start harvests without any delay)
%      'TimeOffset' = offset from server time for converting start times and harvested data time 
%         stamps to local time (numeric; default = 0 for no offset)
%      'FileRaw' = fully qualified filename for saving or appending raw Data Turbine channel data
%         (i.e. DTstruct file) (string; '' to disable saving raw data)
%      'VariableRaw' = variable name for saving the raw channel data (string; default = 'dts_raw')
%      'FileAligned' = fully qualified filename for saving aligned channel structures 
%         (string; '' to disable saving aligned structures)
%      'VariableAligned' = variable name for saving the aligned channel structure (string, default = 'dts_aligned')
%      'Workflow' = MATLAB code to evaluate following a successful data harvest to perform workflow actions
%         (string; optional)
%      'LogOption' = options for logging a summary of operations:
%         'file' = save a log of operations to a variable 'log' in the 'FileRaw' file (default)
%         'email' = email a log of operations the the address specified in 'Email' (see 'sendmail' help for setup)
%         'file,email' = save a log to both disk and email
%         '' = do not log operations
%      'Email' = email address to use if 'LogOption' includes an 'email' option
%      'Console' = option to display status messages on the computer console:
%         'all' = display all harvest status messages (default)
%         'error' = only display error messages
%         '' = do not display console messages
%      'LastDate' = date of last data retrieved (MATLAB serial data or date string; use [] to request 
%         all available data on the first harvest)
%      'Log' = harvest operations log (automatically assigned by DTharvest.m)
%
%notes:
%  1) inputs fn, period and time_offset are provided for convenience when instantiating an options
%     structure for DTharvest.m; if they are not used then corresponding fields should be filled 
%     in manually before calling DTharvest.m, along with other options
%  2) date/time patterns within square brackets can be included in FileRaw and FileAligned
%     to automate date-based file management (e.g. 'met_[yyyymmdd].mat', where yyyymmdd is converted
%     to the current date when the harvest is run)
%  3) Workflow code can contain tokens for FileRaw, VariableRaw, FileAligned and VariableAligned e.g.
%     'vars = load(''[FileAligned]''); data = vars.[VariableAligned]; plot(data.Date,data.Channel1,'bd');'
%  4) Workflow code can also reference the variables 's_aligned' and 's_raw' containing aligned and raw
%     channel structures from DTlatest.m, resp.
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
%last modified: 09-May-2013

%set default filename if omitted
if exist('fn','var') ~= 1
   fn = '';
end

%set default period if omitted
if exist('period','var') ~= 1
   period = 1;
end

%set default time_offset if omitted
if exist('time_offset','var') ~= 1
   time_offset = 0;
end

%initialize structure with default options
options = struct( ...
   'Period',period, ...
   'StartTime','', ...
   'TimeOffset',time_offset, ...
   'FileRaw',fn, ...
   'VariableRaw','dts_raw', ...
   'FileAligned',fn, ...
   'VariableAligned','dts_aligned', ...
   'Workflow','', ...
   'LogOption','file', ...
   'Email','', ...
   'Console','all', ...
   'LastDate',[], ...
   'Log',[]);
