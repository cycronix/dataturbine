function setaxesoptions(ha, opts)
% SETAXESOPTIONS Set options for a particular axes object based on a cell array.
%
% SETAXESOPTIONS(HA, OPTS) takes the java.util.Properties object OPTS
%  which defines the properties for the axes object 
%  represented by the handle HA.  The properties should be all lower case.
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
entryIter = opts.entrySet.iterator;

while entryIter.hasNext,
	keyValue = entryIter.next;
	key = char(keyValue.getKey);
	value = char(keyValue.getValue);
	try,
		switch key,
			case {'title', 'ylabel', 'xlabel'},
				% must set through sub-handle
				set(get(ha, key), 'string', value, 'interpreter', 'none');
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
