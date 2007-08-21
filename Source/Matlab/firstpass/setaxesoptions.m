function setaxesoptions(ha, opts)
% SETAXESOPTIONS Set options for a particular axes object based on a cell array.
%
% SETAXESOPTIONS(HA, OPTS) takes the cell array 'opts', which should be
%  m x 2, and for each row sets the key in the first column to the value
%  in the second column, for the properties for the axes object 
%  represented by the handle HA.
%
% Some properties are handled specially.  If the target property is numerical,
%  the input will be converted to a number before being set.  If the 
%  target property is a handle, the property will be set to the 'string' 
%  property on the object represented by that handle.
%
% Lastly, if the property is 'size', it will be set on the parent's 'position'
%  property, so that the width and height are changed.
%

%
% 2007/06/01  WHF  Created.
%

hf = get(ha, 'parent');
for ii = 1:size(opts,1),
	key = lower(opts{ii,1});
	value = opts{ii,2};
	try,
		switch key,
			case {'title', 'ylabel', 'xlabel'},
				% must set through sub-handle
				set(get(ha, key), 'string', value);
			case 'size',
				sz = str2num(value);
				set(hf, 'position', [0 0 sz(:)']);
				% set to allow changing fig size in output:
				set(hf, 'paperpositionmode', 'auto'); 				
			otherwise
				% set directly
				if isnumeric(get(ha, key)),
					set(ha, key, str2num(value));
				else
					set(ha, key, value);
				end
		end
	catch,
		le = lasterror;
		warning('plotter:bad_key_value', ...
				'Problem setting key (%s) / value (%s):\n\t%s', ...
				key, value, le.message ...
		);
	end
end
