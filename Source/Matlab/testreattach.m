% test detach/reattach bug

function testreattach(host)

  if(nargin < 1) host = 'localhost';
  end
  
% Source
  ncache = 1;
  narchive = 10000000;
  nput = 1;
  
  src = com.rbnb.sapi.Source(ncache,'create',narchive);
  src.OpenRBNBConnection(host, 'mySource'); 
  
  % use the simple put/get methods
  for i=1:nput
      rbnb_put(src, 'x', i, i, 0);
  end
  
% Sink
  snk = rbnb_sink(host,'mySink');
  
  [gdat gtim gnam] = rbnb_get(snk, 'mySource/x', 1, narchive);

  if(1)
    src.Detach();                % try detach  
    src = rbnb_source(host, 'mySource', ncache, 'append', narchive); 
  end
  
%  keyboard
  
% append some more data
  for i=(nput+1):(2*nput)
      rbnb_put(src, 'x', i, i, 0);
  end
  
  [gdat1 gtim gnam] = rbnb_get(snk, 'mySource/x', 1, narchive);
%gdat1

% close and re-open
  src.CloseRBNBConnection(0,1);
  src = com.rbnb.sapi.Source(ncache,'append',narchive);
  src.OpenRBNBConnection(host, 'mySource'); 
  
  [gdat2 gtim gnam] = rbnb_get(snk, 'mySource/x', 1, narchive);
%gdat2
%nput

% all done with RBNB access
  src.CloseRBNBConnection(0,1);  
  snk.CloseRBNBConnection;

  if(length(gdat2) ~= 2*nput) 
	error('Bad data after reattach');
  end

  fprintf('PASS: reatttach test\n');
 
  return

