% test for get oldest absolute data bug

function testoldest(host)

  if(nargin < 1) host = 'localhost';
  end
  
  % source
  src = javaObject('com.rbnb.sapi.Source');
  src.OpenRBNBConnection(host,'Test');
   
  % sink
  snk = javaObject('com.rbnb.sapi.Sink');
  snk.OpenRBNBConnection(host,'testsink');

  % ask for Registration map a couple ways
  cmap = javaObject('com.rbnb.sapi.ChannelMap');
  idx = cmap.Add('c1');
  cmap.PutTime(0,0);
  cmap.PutDataAsString(idx,'d1');
  src.Flush(cmap,1);
  
  cmap.Clear();
  idx = cmap.Add('c2');
  cmap.PutTime(1,0);
  cmap.PutDataAsString(idx,'d2');
  src.Flush(cmap,1);
  
  cmap.Clear();
  cmap.Add('Test/*');
  snk.Request(cmap,0,2,'absolute');
  cmap = snk.Fetch(-1);
  if(cmap.NumberOfChannels == 2) status = 'PASS';
  else                           status = 'FAIL';
  end

  src.CloseRBNBConnection(0, 0);
  snk.CloseRBNBConnection(0, 0);
  
  fprintf([status ': get oldest absolute time test\n']);
return


