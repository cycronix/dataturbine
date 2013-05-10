function [s,msg] = DTalign(chan,time_offset,prefix)
%Aligns channels in a DTstruct returned from DTget to create a unified data structure 
%with a single Date field, named channel fields and duplicate observation records removed
%
%syntax: [s,msg] = DTalign(chan,time_offset,prefix)
%
%input:
%   chan = structure from then 'chan' field of a DTstruct returned from DTget.m (struct; required)
%   time_offset = time offset from server in hours (number; optional; default = 0 for none)
%   prefix = prefix to add to numeric channel names to prevent MATLAB struct field naming errors
%       (string; optional; default = 'channel_', e.g. 'channel_10')
%
%output:
%   s = struct containing a 'Date' field (array of MATLAB serial dates) and fields
%       for each channel containing numeric or character data arrays aligned to 'Date'
%   msg = text of any error message
%
%notes:
%   1) if the entire DTstruct is input as 'chan', the 'chan' field will be extracted automatically
%   2) if fields in chan contain different numbers of observations or vary in frequency, 
%      NaN or ' ' will be added to numeric or string channel data arrays, resp., to align 
%      all observations to the output 'Date' array
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
s = [];
msg = '';

%validate input
if isstruct(chan)
   
   %check for time_offset option
   if exist('time_offset','var') ~= 1 || isempty(time_offset) || ~isnumeric(time_offset)
      time_offset = 0;  %default to no offset if omitted or invalid
   end
            
   %extract chan field if entire DTstruct structure passed in as input
   if isfield(chan,'chan')
      chan = chan.chan;
   end
   
   %validate channel structure
   if isfield(chan,'name') && isfield(chan,'data') && isfield(chan,'time')
      
      %set default prefix if omitted
      if exist('prefex','var') ~= 1
         prefix = 'channel_';
      end
      
      %get all unique time stamps (seconds since 1970) as an array
      sc_all = unique([chan.time]');
      
      %calculate number of records
      numrec = length(sc_all);
      
      %extract channel names and remove unsupported characters for use as structure fields
      chan_names = {chan.name}';
      num_chan = length(chan);
      chan_names = regexprep(chan_names,'[^a-zA-Z0-9_]*','_');
      
      %check for columns starting with number or _
      Inonalpha = regexp(chan_names,'^[0-9_]+.*');
      Iinvalid = find(~cellfun('isempty',Inonalpha));
      
      %generate array of column prefixes
      for cnt = 1:length(Iinvalid)
         chan_names{Iinvalid(cnt)} = [prefix,chan_names{Iinvalid(cnt)}];
      end
      
      %build cell array of output field names
      flds = [{'Date'} ; chan_names];
      
      %calculate values for Date by converting seconds since 1970 to serial date with time_offset
      dt_all = sec2date(sc_all,'1/1/1970',time_offset);
      
      %init data array with Date as column 1 and slots for all channels
      data = [{dt_all} , cell(1,num_chan)];
      
      %init null arrays for numeric and character channel data
      data_null = ones(numrec,1) .* NaN;      
      chardata_null = repmat(' ',numrec,1);
      
      %loop through channels, aligning to master time index
      for cnt = 1:num_chan
         
         %get channel time
         sc = chan(cnt).time';
         
         %get channel data
         chan_data = chan(cnt).data';
         
         %calculate intersect, get match index
         [~,Imatch_all,Imatch] = intersect(sc_all,sc);
         
         %fill in data arrays for matching time indices
         if isnumeric(chan_data)
            data2 = data_null;
            data2(Imatch_all) = chan_data(Imatch);
         else  %character
            data2 = chardata_null;
            data2(Imatch_all) = chan_data(Imatch);
         end
         
         %update structure field
         data{cnt+1} = data2;
         
      end
      
      %convert fieldnames and data array to structure
      s = cell2struct(data,flds,2);
      
   else
      msg = 'invalid input structure';
   end
   
else
   msg = 'a channel structure from DTget is required';
end