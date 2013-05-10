function [ DTgot, nchan ] = DTlist( DT )

%[ DTgot nchan ] = DTget( DT )
%   Get list of channels from DataTurbine. Input and output is 'DT' structure.
%   optional second output param is number of listed channels
%   DT structure parameters (with examples):
%       DT.server = 'localhost';
%       DT.source = '_Metrics';
%       DT.chan = '*';
%
%   On input, DT.chan is a string (or struct array with names).
%   On output, DT.chan is array of DTchan structures, e.g.:
%       chan(1).name = 'MemoryUsed';
%
%   On input, set DT.source to '*' to get list of sources, then
%   On output, DT.source is cell array of source name(s)
%   If multiple sources, channel names will have source/chan parts

% open connections, allocate channel-maps
DTgot = DT;
nchan = 0;
snk = com.rbnb.sapi.Sink();
snk.OpenRBNBConnection(DT.server,'dtList');
cget = com.rbnb.sapi.ChannelMap();

% build fetch-map from source / name(s)
if(ischar(DT.chan))
    cget.Add([DT.source '/' DT.chan]);
else
for i=1:length(DT.chan)
    cget.Add([DT.source '/' DT.chan(i).name]);
end
end

% request and fetch data
snk.RequestRegistration(cget);
cgot = snk.Fetch(60000);    % 60 sec timeout
snk.CloseRBNBConnection();  % done with connect  

nchan = cgot.NumberOfChannels();
nsource=0;
source{1}='';
for i=1:nchan
    % parse chan name from source/name
    scname = char(cgot.GetName(i-1));
    if(strcmp(DT.source, '*'))
        chan(i).name = scname;      % keep source/name for multiple sources
    else
        cname = regexp(scname, '([^/]+$)', 'tokens');
        chan(i).name = char(cname{1});
    end
    
    sname = regexp(scname, '(^[^/]+)', 'tokens');
    sname = char(sname{1});
    if(~max(strcmp(sname,source)))
        nsource = nsource + 1;
        source{nsource} = sname;
    end
    
    % assign meta fields
    chan(i).mime = char(cgot.GetMime(i-1));
    chan(i).meta = char(cgot.GetUserInfo(i-1));
end

% clean up
if(nchan)
    DTgot = DT;
    DTgot.chan = chan;
    if(length(source) > 1)
        DTgot.source = source;
    end
end

end
    
