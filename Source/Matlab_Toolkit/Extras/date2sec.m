function sc = date2sec(dt,refdate,time_offset)
%Calculates cumulative seconds past a reference date from a MATLAB serial date
%
%syntax: sec = date2sec(dt,refdate,time_offset)
%
%input:
%   dt = array of date strings or MATLAB serial dates from datenum - required
%   refdate = numeric or string reference date at which sc = 0 
%       (e.g. '1/1/1970' for Unix time) - required
%   time_offset = time offset in hours for local<->UTC conversion 
%       (number; optional; default = 0 for none)
%
%output:
%   sc = seconds since the reference date
%
%notes:
%   1) seconds will be fixed at 5 decimal places to remove arbitrary digits from floating-point math
%   2) time_offset will be converted to days and added to dt to support conventional UTC corrections
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
sc = [];

%check for required arguments
if nargin >= 2
   
   %check for time_offset option
   if exist('time_offset','var') ~= 1 || isempty(time_offset) || ~isnumeric(time_offset)
      time_offset = 0;  %default to no offset if omitted or invalid
   end
      
   %calculate date offset in days
   if time_offset ~= 0
      dt_offset = time_offset ./ 24;
   else
      dt_offset = 0;
   end
   
   %convert string refdate to numeric date
   if ischar(refdate)
      try
         refdate = datenum(refdate);
      catch
         refdate = [];
      end
   end
   
   %convert string dt to numeric date
   if ~isnumeric(dt)
      if iscell(dt)
         dt = char(dt(:));
      end
      try
         dt = datenum(dt);
      catch
         dt = [];
      end
   end
   
   %convert date to seconds, offset by time_offset in days (note: 86400 = 24hr/day * 60min/hr * 60sec/min)
   if ~isempty(refdate) && ~isempty(dt)
      sc = (dt - (refdate + dt_offset)) .* 86400;
   end
   
end