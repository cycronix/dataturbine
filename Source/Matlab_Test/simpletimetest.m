
  x=1:20;
  
  src = com.rbnb.sapi.Source;
  src.OpenRBNBConnection('localhost', 'mySource');
  src.SetRingBuffer(100, 'create', 100);

  cmap = com.rbnb.sapi.ChannelMap;
  cmap.Add('x');
  
  tic;
  cmap.PutTime(0, 0);
  cmap.PutDataAsFloat64(0, x);
  src.Flush(cmap, 1);

  writerate =  length(x) * 8 / toc
  src.CloseRBNBConnection(0,1);   % keep archive
 


