% test archiving

function testarc(host)

  if(nargin < 1) host = 'localhost';
  end
  
% Source
  ncache = 100;
  narchive = 2*ncache;
  
  src = rbnb_source(host, 'mySource', ncache, 'create', narchive*2);
  
  % use the simple put/get methods
  for i=1:narchive
      rbnb_put(src, 'x', i, i, 1);
  end
  
% Sink
%pause(1); % need rbnb_sync, source gets ahead of archive write
  
  snk = rbnb_sink(host,'mySink');
  snk.GetChannelList('...');    % see what is in there
  
  [gdat gtim gnam] = rbnb_get(snk, 'mySource/x', 1, narchive);
  
  if(strcmp(gnam, 'mySource/x') ~= 1) 
      src.CloseRBNBConnection(0,0);
      snk.CloseRBNBConnection;
      error('Channel name mismatch');
  end
  if(size(gdat) < narchive)
      src.CloseRBNBConnection(0,0);
      snk.CloseRBNBConnection;
      error('Fetched wrong amount of data');
  end
  if(max(abs(gdat-(1:narchive))) > 0) 
      src.CloseRBNBConnection(0,0);
      snk.CloseRBNBConnection;
      error('Channel data mismatch');
  end

% try closing and re-loading archive
  src.CloseRBNBConnection(0,1);
%  pause(.2);                 % need rbnb_sync or synchronous close

  narchive2 = 5*ncache;
  src = rbnb_source(host, 'mySource', ncache, 'append', narchive + narchive2);
  
% append some more data
  for i=1:narchive2
      rbnb_put(src, 'x', i+narchive, i+narchive, 1);
  end
  
  [gdat gtim gnam] = rbnb_get(snk, 'mySource/x', 1+narchive, narchive2);

%  length(gdat)
%  gdat
  if(length(gdat) < narchive2)
      src.CloseRBNBConnection(0,0);
      snk.CloseRBNBConnection;
      error('Failed to get data (on append/reload)');
  end

  check = narchive + (1:narchive2);
  if(max(abs(gdat-check)) > 0)
      src.CloseRBNBConnection(0,0);
      snk.CloseRBNBConnection;
      error('Bad data (on append/reload)');
  end
  if(max(abs(gtim-check)) > 0)
      src.CloseRBNBConnection(0,0);
      snk.CloseRBNBConnection;
      error('Bad time (on append/reload)');
  end
  
  [gdat gtim gnam] = rbnb_get(snk, 'mySource/x', 1, narchive+narchive2);
%  length(gdat)
%  narchive
%  narchive2
  if(length(gdat) < narchive/2+narchive2)
      src.CloseRBNBConnection(0,0);
      snk.CloseRBNBConnection;
      error('Failed to get all data (on append/reload)');
  end

% all done with RBNB access
  src.CloseRBNBConnection(0,0);  % delete archive is no-op ?
  snk.CloseRBNBConnection;
  
  fprintf('PASS: archive test\n');

%  pause(.1) % wait for close, should be synchronous
return

