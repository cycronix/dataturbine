function dt = sec2date(sc,refdate,time_offset)
%Calculates a MATLAB serial date from cumulative seconds past a reference date
%
%syntax: dt = sec2date(sc,refdate,time_offset)
%
%input:
%   sc = array of seconds past the base date (numeric; required)
%   refdate = numeric or string reference date at which sc = 0 
%       (e.g. '1/1/1970' for Unix time) - required
%   time_offset = time offset in hours for local<->UTC conversion 
%       (number; optional; default = 0 for none)
%
%output:
%   dt = array of MATLAB serial dates (numeric)
%
%notes:
%   1) time_offset will be converted to days and added to dt to support conventional UTC corrections
%      (e.g. use time_offset = -5 to adjust a date in UTC to EST (UTC-05:00), and use 
%       time_offset = 5 to convert from EST to UTC)
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
dt = [];

%check for required arguments
if nargin >= 2
   
   %check for time_offset option
   if exist('time_offset','var') ~= 1 || isempty(time_offset) || ~isnumeric(time_offset)
      time_offset = 0;  %default to no offset if omitted or invalid
   end
      
   %calculate date offset
   if time_offset ~= 0
      dt_offset = time_offset ./ 24;
   else
      dt_offset = 0;
   end
   
   %convert string date to numeric
   if ischar(refdate)
      try
         refdate = datenum(refdate);
      catch
         refdate = [];
      end
   end
   
   %convert seconds to MATLAB serial date, where 86400 = 24hr/day * 60min/hr * 60sec/min
   if ~isempty(refdate)
      dt = sc ./ 86400 + (refdate + dt_offset);
   end
   
end