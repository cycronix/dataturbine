function offset = getUTCOffset()
%Calculates the offset of the computer clock from UTC based on time zone settings
%
%syntax: offset = getUTCOffset()
%
%input:
%   none
%
%output:
%   offset = UTC offset in hours
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

%calculate current time in seconds since 1970 UTC using the Java currentTimeMillis method
sec_utc = java.lang.System.currentTimeMillis() ./ 1000;

%calculate date from sec_utc
dt_utc = sec2date(sec_utc,'1/1/1970');

%get system date in local time
dt_local = now;

%calculate offset in hours, rounding to 1 decimal place
offset = round((dt_local - dt_utc) .* 240) ./ 10;