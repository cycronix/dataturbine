function [ DTgot, nchan ] = DTget( DT )

%[ DTgot nchan ] = DTget( DT )
%   Get data from DataTurbine. Input and output is 'DT' structure.
%   optional second output param is number of fetched channels
%   DT structure parameters (with defaults):
%       DT.server = 'localhost';
%       DT.source = '_Metrics';
%       DT.start = 0;
%       DT.duration = 0;
%       DT.reference = 'newest';
%       DT.chan = '*';
%
%   On input, DT.chan is a string (or struct array with names).
%   On output, DT.chan is array of DTchan structures, e.g.:
%       chan(1).name = 'MemoryUsed';
%       chan(1).time = 0;
%       chan(1).data = 0;
%       chan(1).type = 'int64';
%       chan(1).mime = 'binary';
%       chan(1).meta = 'null';

% open connections, allocate channel-maps
DTgot = DT;
nchan = 0;
snk = com.rbnb.sapi.Sink();
snk.OpenRBNBConnection(DT.server,'dtSink');
cget = com.rbnb.sapi.ChannelMap();
cgot = com.rbnb.sapi.ChannelMap();

% build fetch-map from source / name(s)
if(ischar(DT.chan))
    cget.Add([DT.source '/' DT.chan]);
else
for i=1:length(DT.chan)
    cget.Add([DT.source '/' DT.chan(i).name]);
end
end

% request and fetch data
snk.Request(cget,DT.start,DT.duration,DT.reference);
cgot = snk.Fetch(60000);    % 60 sec timeout
snk.CloseRBNBConnection();  % done with connect

tmin=99999999999;
tmax=0;
chan(1).name='undefined';

% cycle through channel(s), populate result structure
nchan = cgot.NumberOfChannels();
for i=1:nchan
    % parse chan name from source/name
    sname = char(cgot.GetName(i-1));
    sname = regexp(sname, '([^/]+$)', 'tokens');
    chan(i).name = char(sname{1});
    
    % keep track of min/max time to calc duration
    times = cgot.GetTimes(i-1);
    if(tmin > times(1)) tmin = times(1);
    end
    if(tmax < times(end)) tmax = times(end);
    end
    
    % assign time, data, meta fields
    chan(i).time = times';
    [dat, typ] = dt_getdata(cgot,i-1);
    chan(i).data = dat;
    chan(i).type = typ;
    chan(i).mime = char(cgot.GetMime(i-1));
    chan(i).meta = char(cgot.GetUserInfo(i-1));
end

% clean up
if(nchan)
    DTgot = DT;
    DTgot.chan = chan;
    DTgot.start = tmin;
    DTgot.duration = tmax - tmin;
%   DTgot.reference = 'absolute';   % actual
end

end

% local function to get data from channel map
function [ dat, typs ] = dt_getdata(cmap, idx)

    typ = cmap.GetType(idx);
    cm = com.rbnb.sapi.ChannelMap;
    
% cast to appropriate datatype
    if     (typ == cm.TYPE_FLOAT64) 
      dat = cmap.GetDataAsFloat64(idx)';     % transpose col to row vector
      typs = 'float64';
    elseif (typ == cm.TYPE_STRING) 
      dat = char(cmap.GetDataAsString(idx));  % Java string to Matlab char-array
      typs = 'string';
    elseif     (typ == cm.TYPE_FLOAT32) 
      dat = cmap.GetDataAsFloat32(idx)';
      typs = 'float32';
    elseif     (typ == cm.TYPE_INT32)
      dat = cmap.GetDataAsInt32(idx)';
      typs = 'int32';
    elseif     (typ == cm.TYPE_INT64)
      dat = cmap.GetDataAsInt64(idx)';
      typs = 'int64';
    elseif     (typ == cm.TYPE_INT16)
      dat = cmap.GetDataAsInt16(idx)';
      typs = 'int16';
    else
      dat = cmap.GetData(idx);               % everything else byte-array
      typs = 'int8';
    end    
 
end
    

