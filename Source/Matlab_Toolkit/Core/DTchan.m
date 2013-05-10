function chan = DTchan()

% chan = DTchan()
%   Return default DTchan structure:
%       chan(1).name = 'MemoryUsed';
%       chan(1).time = 0;
%       chan(1).data = 0;
%       chan(1).type = 'int64';
%       chan(1).mime = 'binary';
%       chan(1).meta = 'null';

chan.name = 'MemoryUsed';
chan.time = 0;
chan.data = 0;
chan.type = 'int64';
chan.mime = 'binary';
chan.meta = 'null';

end
