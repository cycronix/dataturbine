function [ DTgot, nchan ] = DTnext( DT, delay, timeout )

%[ DTgot nchan ] = DTnext( DT [, delay, timeout] )
%  Get 'next' data after previous fetch without overlap.
%  Initialize with DT.reference 'newest', 'oldest', or 'absolute',
%  after that call with DT = DTprevious.
%
%  Parameters:
%  DT - DT structure defining what to get
%  delay - polling interval to check for new data
%  timeout - give up waiting after this long

if(nargin < 2) delay = 1;             % default poll once per sec
end
if(nargin < 3) timeout = 1000*delay;  % default long time but not forever
end

duration = DT.duration;             % remember this
smidge = 0.0001;                    % no overlap

if(~strcmp(DT.reference,'next'))   % initialize
    [DTgot, nchan] = DTget(DT);
    DTgot.reference = 'next';
    DTgot.start = DTgot.start + DTgot.duration + smidge;
    DTgot.duration = duration;      % regardless
    return;
end

imax = timeout / delay;
dget = DT;

for i=1:imax
    [dget, nchan] = DTget(dget);
    if(nchan)    % got something new
%        [dget.chan.time dget.chan.data ]
        DTgot = dget;
        DTgot.start = dget.start + dget.duration + smidge;
        DTgot.duration=duration;            % regardless
        return;
    end
    pause(delay);
end

DTgot = DT;      % notta
nchan = 0;
return;

