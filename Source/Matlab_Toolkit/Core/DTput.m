function nput = DTput( DT, ncache, narchive, amode )

%nput = DTput( DT [, ncache, narchive, amode] )
%
%   Flush data to DataTurbine. Input is 'DT' structure.
%   DT structure parameters:
%       DT.server, e.g. 'localhost';
%       DT.source, e.g. 'mysource';
%       DT.chan(), as follows:
%
%   DT.chan is struct array with names and data, e.g.:
%       chan(1).name = 'mychan';
%       chan(1).data = 0;
%
%   Optional ringbuffer-settings:
%       ncache - cache frames, default=10
%       narchive - archive frames, default=1000000
%       amode - archive mode, 'none', 'create', 'append' (default)
%
%   Notes:
%       Timestamps are all current time, zero duration
%
%       Call DTput() with no arguments to disconnect. Otherwise
%       re-uses prior connection for efficiency.

persistent src sname;
keepConnection=1;
nput=0;

% cleanup/retain connection

if(nargin < 1)  % no-argument:  clean up connection
    try
        'detaching connection'
        src.Detach();
    catch
    end
    return;
    
elseif(ischar(DT)) 
    if(strcmp(DT,'detach'))
        'detaching connection'
        src.Detach();
    elseif(strcmp(DT,'close'))
        'closing connection'
        src.CloseRBNBConnection();
    else
        'unrecognized command'
    end
    return;
end

doopen=1;
if(keepConnection && strcmp(DT.source,sname))
    try
        if(src.VerifyConnection) doopen=0;
        end
    catch
    end
end

if(doopen) 
    fprintf('(re)opening source\n');
    if(nargin < 2) ncache = 10;  % not meaningful if reconnect every put
    end
    if(nargin < 3) narchive = 1000000;
    end
    if(nargin < 4) amode = 'append';
    end
    src = com.rbnb.sapi.Source(ncache,amode,narchive);
    src.OpenRBNBConnection(DT.server,DT.source);
    sname = DT.source;
end

cput = com.rbnb.sapi.ChannelMap();

% cput.PutTime(DT.start,DT.duration);   % default to 'now'?
for i=1:length(DT.chan)
    c = DT.chan(i);
    idx = cput.Add([c.name]);
    
    if     (isa(c.data,'double')) cput.PutDataAsFloat64(idx, c.data);
    elseif (isa(c.data,'char'))   cput.PutDataAsString (idx, c.data);      
    else                          cput.PutDataAsInt8   (idx, c.data);    
    end 
end

nput = src.Flush(cput);

if(~keepConnection) 
    src.Detach();   % not maintaining src will chop up archive 
end

end

