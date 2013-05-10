function dts = DTcombine(dts_array)
%Combines channel data retrieved in multiple requests from a Data Turbine server
%
%syntax: dts = combine_channels(dts_array)
%
%input:
%   dts_array = cell array of DTstruct structures returned from DTget
%
%output:
%   dts = unified DTstruct structure with concatenated channel arrays and
%      metadata from the first structure
%
%notes:
%   1. channels will be matched by name, and 'time' and 'data' field arrays
%      from the multiple structures will be concatenated in order
%   2. if structures are included from multiple Data Turbine sources with 
%      overlapping channel names, the source will be appended to the channel
%      name for redundant channels to prevent inappropriate concatenation
%   3. data with overlapping times will not be removed - use DTalign.m to 
%      produce a unified data set with aligned data arrays from the 'dts' output
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
dts = [];

if nargin == 1 && iscell(dts_array)
   
   %init combined channel structure using first dts_multi structure
   dts = dts_array{1};
   chan_all = dts.chan;
   
   %get channel names for matching
   chan_names_all = {chan_all.name}';
   
   %init multi-source flag
   multisource = 0;
   
   %loop through other dimensions of dts_multi concatenating time and data arrays
   for dim = 2:length(dts_array)
      
      %get channel structure and channel names for next dimenion
      dts_next = dts_array{dim};
      
      %validate structure
      if isstruct(dts_next) && isfield(dts_next,'chan')
         
         %get channel structure and channel names
         chan = dts_next.chan;
         chan_names = {chan.name}';
         
         %check for different source than first DTstruct, generate suffix
         if ~strcmp(dts.source,dts_next.source)
            chan_suffix = ['_',dts_next.source];
         else
            chan_suffix = '';
         end
         
         %loop through channels matching to chan1 channel names
         for cnum = 1:length(chan_names);
            
            %generate channel name (adding suffix if source different than first structure)
            chan_name = [chan_names{cnum},chan_suffix];
            
            %get channel name match index
            Imatch = find(strcmp(chan_name,chan_names_all));
            
            %append time and data arrays to existing channels or create new channel if unmatched
            if ~isempty(Imatch)
               
               %append to existing channel               
               Imatch = Imatch(1);  %use first match to channel name
               
               %append time array
               t = chan(cnum).time;
               chan_all(Imatch).time = [chan_all(Imatch).time t];
               
               %append data array
               data = chan(cnum).data;
               chan_all(Imatch).data = [chan_all(Imatch).data data];
               
            else  %umatched - add to chan_all
               
               %set multi-source flag
               multisource = 1;
               
               %add new dimension to chan_all and add new channel name to match array
               chan_new = chan(cnum);  %extract channel structure fields
               chan_new.name = chan_name;  %update name
               chan_all(length(chan_all)+1) = chan_new;  %add to chan_all
               chan_names_all = [chan_names_all ; chan_name];   %#ok<AGROW> -- predimensioning impractical so skip warning
               
            end
            
         end
         
      end
      
   end
   
   %generate output structure with combined channel data and metadata from first dimension of dts_multi
   dts.chan = chan_all;
   
   %check for multiple sources
   if multisource == 1
      dts.source = 'multiple';
   end
   
end