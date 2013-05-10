function str2 = fillDateTokens(str)
%Replaces date/time field tokens in strings with current date/time from the system clock
%
%syntax: str2 = fillDateTokens(str)
%
%input:
%  str = string containing date/time tokens in square brackets (e.g. 'met_data_[yyyymmdd].mat')
%
%output:
%  str2 = updated string with date/time tokens resolved
%
%notes:
%  1) date/time tokens must be enclosed in square brackets (e.g. 'loggerfile_[yyyymmdd].mat')
%  2) multiple date/time tokens can be included in str (e.g. 'loggerfile_[yyyymmdd]_[HHMM].mat')
%  3) see 'datestr' help for supported tokens (e.g. yyyy = year, mm = numeric month,
%     mmm = 3-letter month, dd = day, HH = hour, MM = minute)
%  4) if no date tokens are present or if calls to datestr() return an error for text within
%     brackets the unmodified string will be returned as str2
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
str2 = '';

if nargin == 1 && ~isempty(str)
   
   %get start/end indices of date/time patterns in str
   [Idstart,Idend] = regexp(str,'\[[-a-zA-Z]+\]');
   num_pat = length(Idstart);
   
   %check for date/time pattern matches
   if num_pat == 0
      
      %no matches - return original string
      str2 = str;
      
   else
      
      %add text before first token
      if Idstart(1) > 1
         str2 = str(1:Idstart(1)-1);
      end
      
      %loop through patterns filling in date/time tokens
      for cnt = 1:num_pat
         
         %extract pattern, resolve and append to output
         dpattern = str(Idstart(cnt)+1:Idend(cnt)-1);
         
         %resolve date/time component
         try
            dstr = datestr(now,dpattern);
         catch
            dstr = dpattern;  %fall back to original string if not valid datestring
         end
         
         %add to output
         str2 = [str2,dstr];
         
         %add intervening or terminal text
         if cnt < num_pat
            %add intervening text between patterns
            str2 = [str2,str(Idend(cnt)+1:Idstart(cnt+1)-1)];
         elseif Idend(end) < length(str)
            %add terminal text
            str2 = [str2,str(Idend(end)+1:end)];
         end
         
      end
      
   end

end