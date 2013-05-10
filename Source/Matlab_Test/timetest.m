
function [writerate] = timetestsize(payload)

  host = 'localhost';
  nitems = payload/8;
  x=1:nitems;
  if(payload < 1000) niter = 2000;
  elseif (payload < 10000) niter = 1000;    
  else                niter = 400;
  end
  
  src = com.rbnb.sapi.Source;
  src.OpenRBNBConnection(host, 'mySource');
  src.SetRingBuffer(niter, 'none', 0);
  
  snk = com.rbnb.sapi.Sink;
  snk.OpenRBNBConnection(host,'mySink');
  
% flush
  cmap = com.rbnb.sapi.ChannelMap;
  ix = cmap.Add('x');
  
  tic;
%  cmap.PutTime(0,1);
  for i=1:niter
     cmap.PutTime(i, 1);
     cmap.PutDataAsFloat64(ix, x);
     src.Flush(cmap, 0);
  end
  src.Flush(cmap, 1);
  dt = toc;
  writerate = niter * length(x) * 8 / dt;
  return        % return here for just write test
  
% fetch
  cmap_get = com.rbnb.sapi.ChannelMap;
  cmap_get.Add(strcat(char(src.GetClientName),'/x'));
  
  tic;
  for i=1:niter
     snk.Request(cmap_get, i, 1, 'absolute');
     cmap = snk.Fetch(10000);    
     if(cmap.NumberOfChannels < 1)
        error('FAIL: rbnb_get failed to get channel');
     end
%     dat = cmap.GetDataAsFloat64(0);
%     tim = cmap.GetTimes(0);

%    [y t] = rbnb_get(snk, 'mySource/x', i, 1, 'absolute');
  end
  dt = toc
  readrate = niter * length(x) * 8 / dt

  src.CloseRBNBConnection(0,0);
  snk.CloseRBNBConnection(0,0);
return

