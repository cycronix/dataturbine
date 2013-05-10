% simple script that gets single frame from testput

% setup the DT structure:
dget.server = 'localhost';
dget.source = 'mysource';
dget.start = 0;
dget.duration = 0;          % single frame
dget.reference = 'newest';
dget.chan = '*';

% get it
dgot = DTget(dget);

fprintf('what we got:\n');
dgot
dgot.chan(:).name
dgot.chan(:).data