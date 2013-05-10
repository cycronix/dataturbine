% subscribe to new data (no overlaps)

niter = 100;          % iterations to fetch
delay = 60;           % check once per minute

% setup the DT structure:
dget.server='localhost';    	% address of DT server (enter actual IP)
dget.source='TroutBog';         % this source
dget.start=0;                   % align with newest
dget.duration=3600;             % hour of data each fetch
dget.reference='newest';        % start with most recent
dget.chan='*';                  % all channels 

% crawl thru data and print status
for i=1:niter
    [dget, nchan] = DTnext(dget,delay);
    if(nchan)
        fprintf('%s, nchan: %d, npts: %d, time: %.0f\n', dget.source, ...
            length(dget.chan), length(dget.chan(1).data), dget.start);
    else
        fprintf('no data\n');
    end
end


