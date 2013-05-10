% simple script that puts single frame of data

% setup the Channel array to put:
chan(1).name = 'c1';
chan(1).data = 1;

chan(2).name = 'c2';
chan(2).data = 2;

% setup the DT structure:
dput.server = 'localhost';
dput.source = 'mysource';
dput.chan = chan;

% put it
nput = DTput(dput);

fprintf('what we put:\n');
dput
dput.chan(:).name
dput.chan(:).data