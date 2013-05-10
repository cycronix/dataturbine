function hf = plotter(times, x, opts)
% PLOTTER  Simple MATLAB data plotting routine.
%
% PLOTTER(T, X, OPTS) plots the data in cell array X as a function of time T.
%
% OPTS is a java.util.Properties object.  As an example of using custom 
%  properties, this function supports the 'linecolor' property.
%
% Regardless of whether custom properties are used, similar functions should
%  pass the options onto SETAXESOPTIONS.
%

%
% 2007/05/31  EMF  Created.
% 2007/05/31  WHF  Added time support, labelling.
% 2007/06/11  WHF  Simplified options support.
%

% Get the time values for this line.  The line will be plotted as if it 
%  started at time zero and ended at a time equal to the request duration.
ti = times{1}(1);
t = times{1} - ti;

% Create a figure, not visible on the desktop, with a white background.
%  'InvertHardCopy', 'off' is necessary to allow axes backgrounds to come
%  through on the image.
hf = figure('Visible', 'off', 'inverthardcopy', 'off', 'color', [1 1 1]);
% Get a handle to the current axes:
ha = gca;
% Get the color order property for line color:
co = get(ha, 'colororder');

for ii = 1:length(x),
	% See if a linecolor input property exists, if so, use it, if not, use
	%  colororder from the axes.
	lc_str = opts.get('linecolor');
	if (lc_str), lc = str2num(lc_str); opts.remove('linecolor');
	else lc = co(ii, :); end

	% Plot the line in the desired color:
	plot(t, x{ii}, 'color', lc);
	hold on;
end

hold off;

% Turn grid on by default, can be overridden (in setaxesoptions):
set(ha, 'XGrid','on','YGrid','on'); 

% By default, we label the x-axis with the current date and time.  This 
%  can be overridden (in setaxesoptions).
% Convert RBNB time to millis since epoch:
d = java.util.Date(ti*1e3);
xlabel(char(d.toString))

% Set general MATLAB properties for the axes object:
setaxesoptions(ha, opts);

