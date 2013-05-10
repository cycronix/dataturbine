% simple script that loops plotting newest Metrics data (overlaps data)

% setup the DT structure:
dget.server = 'localhost';
dget.source = '_Metrics';
dget.start = 0;
dget.duration = 60;
dget.reference = 'newest';
dget.chan = 'MemoryUsed';

% loop for a minute:
for i=1:60
    dgot = DTget(dget);
    tref = dgot.chan(1).time(1);
    plot(dgot.chan(1).time - tref, dgot.chan(1).data);
    pause(1);
end
