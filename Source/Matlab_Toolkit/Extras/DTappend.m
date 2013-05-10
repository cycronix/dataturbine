function [dts,msg] = DTappend(dts_old,dts_new,varname,saveoption)
%Appends channel data retrieved from DTget or DTlatest to existing channel data in a variable or file
%
%syntax: [dts,msg] = DTappend(dts_old,dts_new,varname,saveoption)
%
%input:
%   dts_old = DTstruct from DTget or DTlatest, or a fully qualified filename for a .mat file
%      containing a DTstruct to append to (struct or string filename; required)
%   dts_new = DTstruct to append (struct; required)
%   varname = variable containing the dts_old structure to append to if dts_old is a filename
%      (string; optional; default = 'dts')
%   saveoption = option to save the combined DTstruct if dts_old is a filename (integer; 
%       0 = no, 1 = yes/default, ignored if dts_old is a struct)
%
%output:
%   dts = unified DTstruct structure with concatenated channel arrays and metadata from the first structure
%   msg = text of any error message
%
%notes:
%   1. if dts_old is a filename and it does not exist, dts_new will be saved as varname
%      to create the file
%   2. channels will be matched by name, and 'time' and 'data' field arrays
%      from the multiple structures will be concatenated in order
%   3. if structures are included from multiple Data Turbine sources with 
%      overlapping channel names, the source will be appended to the channel
%      name for redundant channels to prevent inappropriate concatenation
%   4. data with overlapping times will not be removed - use DTalign.m to 
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
%last modified: 05-Apr-2013

%init output
dts = [];
msg = '';

if nargin >= 2
   
   %supply default varname if omitted
   if exist('varname','var') ~= 1
      varname = 'dts';
   end
   
   %supply default saveoption if omitted
   if exist('saveoption','var') ~= 1
      saveoption = 1;
   end
   
   %validate dts_new
   if isstruct(dts_new) && isfield(dts_new,'chan')
      
      %check for filename, load variable
      if ischar(dts_old)
         
         %try to load variable from file
         fn = dts_old;
         dts_old = [];
         
         if exist(fn,'file') == 2
            try
               vars = load(fn);
            catch e
               vars = struct('null','');
               msg = ['error loading variable: ',e.message];
            end
         else
            vars = struct('null','');
         end
         
         %check for matching variable
         if isfield(vars,varname)
            dts_old = vars.(varname);
         end

      else
         fn = '';
      end
         
      %chack for valid dts_old
      if isstruct(dts_old) && isfield(dts_old,'chan')                  
         dts = DTcombine({dts_old dts_new});  %combine channel data
      else         
         dts = dts_new;  %use new data         
      end
         
      %check for valid data and save if specified
      if isempty(dts)
         
         msg = 'an error occurred combining the structures using ''DTcombine.m''';         
         
      elseif saveoption == 1 && ~isempty(fn)
         
         %generate structure for saving dts as a named variable
         output = struct(varname,dts);   %#ok<NASGU> -- omit spurious mlint warning
         
         %save file, appending variable
         if exist(fn,'file') == 2
            save(fn,'-struct','output','-append')  %append
         else
            save(fn,'-struct','output')  %create
         end
         
      end
         
   end
   
end