function hf = plotter(times, x, opts)
% PLOTTER  Simple MATLAB data plotting routine.
%

%
% 2007/05/31  EMF  Created.
% 2007/05/31  WHF  Added time support, labelling.
%

% Create a figure, not visible on the desktop, with a white background.
%  'InvertHardCopy', 'off' is necessary to allow axes backgrounds to come
%  through on the image.
hf = figure('Visible', 'off', 'inverthardcopy', 'off', 'color', [1 1 1]);
ha = gca;
co = get(ha, 'colororder');
ti = times{1}(1);
t = times{1} - ti;
for ii = 1:length(x),
	lc_index = find(strcmpi({opts{:, 1}}, 'linecolor'));
	if isempty(lc_index),
		lc = co(ii, :);
	else
		lc = str2num(opts{lc_index, 2});
		% Remove linecolor from property set:
		opts = {opts{[1:lc_index-1 lc_index+1:end],1};
				opts{[1:lc_index-1 lc_index+1:end],2}}'; 
	end
		
	plot(t, x{ii}, 'color', lc);
	hold on;
end

hold off;

set(ha, 'XGrid','on','YGrid','on'); % turn grid on by default, can be overridden
% Convert RBNB time to millis since epoch:
d = java.util.Date(ti*1e3);
xlabel(char(d.toString))

setaxesoptions(ha, opts);

