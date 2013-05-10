% crawl thru data (no overlaps)

niter = 10;     % number of iterations to fetch

% setup the DT structure:
dget.server='localhost';
dget.source='_Metrics';
dget.start=0;            
dget.duration=0;
dget.reference='newest';    % start at newest
dget.chan='MemoryUsed';

% crawl thru data and print
for i=1:niter
    [dget, nchan] = DTnext(dget);
    if(nchan)
        fprintf('%s, time: %s, data: %s\n', dget.chan.name,num2str(dget.chan.time),num2str(dget.chan.data));
    else
        fprintf('no data\n');
    end
end


