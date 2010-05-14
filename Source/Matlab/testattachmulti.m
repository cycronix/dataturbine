% test detach/reattach multiple times
% this tests 5/2010 update to grow filesets (vs trim them)
% for the case of a source detaching/reatttaching repeatedly.
% Matt Miller Erigo

function testattachmulti(host)

  if(nargin < 1) host = 'localhost';
  end
  
% Source
  ncache = 1;
  narchive = 1000;
  
  src = com.rbnb.sapi.Source(ncache,'create',narchive);
  src.OpenRBNBConnection(host, 'mySource'); 

  cmap = com.rbnb.sapi.ChannelMap;
  ix = cmap.Add('x');
  cmap.PutTime(0, 0);
  cmap.PutDataAsFloat64(ix, 0);
  src.Flush(cmap, 1);
  
% repeatedly detach, reconnect, put data
  ntry = 50;
  for i=1:ntry
    src.Detach();    
    pause(.01);   % sleepy loop
    src = com.rbnb.sapi.Source(ncache, 'append', narchive);
    src.OpenRBNBConnection(host, 'mySource');
    cmap.PutTime(i, 0);
    cmap.PutDataAsFloat64(ix, i); % append some more data
    src.Flush(cmap, 1);
  end
  
% now fetch data, confirm expected amount is available
  snk = com.rbnb.sapi.Sink;  % sink
  snk.OpenRBNBConnection(host,'mySink');
  
  cmap.Clear;         % new channelMap
  cmap.Add('mySource/x');
  snk.Request(cmap, 0, ntry*2, 'absolute');
  cmap = snk.Fetch(60000);                  % 60 sec timeout

  if(cmap.NumberOfChannels < 1)
    error('FAIL: rbnb_get failed to get channel');
  end

  dat = cmap.GetDataAsFloat64(0);
   
% all done with RBNB access
  src.CloseRBNBConnection(0,0);     % delete archives
  snk.CloseRBNBConnection;

  nexpect = min(ntry,narchive) + 1;
  if(length(dat) ~= nexpect) 
	error('Bad data after reattach');
  end

  fprintf('PASS: attachmulti test!\n');

  return

