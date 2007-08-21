% V2.2
% rbnb archive streaming from oldest test.
% This should only work if either reframing is turned off, or if reframing
% is fixed so that streaming works with it.  It also only works with servers
% that support streaming from oldest (such as V2.2 servers).

function [status] = teststrold(host,nframes)

  if (nargin < 1)
    host = 'localhost';
  end
  if (nargin < 2)
    nframes = 100;
  end

  src = rbnb_source(host,'mySource',nframes,'create',nframes);
  cmapsrc = com.rbnb.sapi.ChannelMap;
  cmapsrc.Add('c1');
  cmapsrc.Add('c2');

  for idx=1:nframes
    cmapsrc.PutTime(idx,1.);
    for chan=1:2
      for idx1=1:10
        data(idx1)=idx+((idx1 - 1)*2 + chan)/20.;
      end
      cmapsrc.PutDataAsFloat64(chan - 1,data);
      data = 0;
    end
    src.Flush(cmapsrc,true);
  end

  src.CloseRBNBConnection(0,1);
  src = rbnb_source(host,'mySource',10,'load',nframes);
  snk = rbnb_sink(host,'mySink');
  cmapsnk = com.rbnb.sapi.ChannelMap;
  cmapsnk.Add(strcat(char(src.GetClientName),'/c1'));
  cmapsnk.Add(strcat(char(src.GetClientName),'/c2'));
  snk.Subscribe(cmapsnk,'oldest');

  for idx=1:nframes
    cmapgot = snk.Fetch(10000);
    if (cmapgot.GetIfFetchTimedOut)
      src.CloseRBNBConnection(0,0);
      snk.CloseRBNBConnection;
      error('FAIL: Timed out waiting for data.');
    end
    if (cmapgot.NumberOfChannels ~= 2)
      src.CloseRBNBConnection(0,0);
      snk.CloseRBNBConnection;
      error('FAIL: Got wrong number of channels.');
    end
  
    for chan=1:2
      gdat = cmapgot.GetDataAsFloat64(chan - 1)';
      gtim = cmapgot.GetTimes(chan - 1)';
      gnam = cmapgot.GetName(chan - 1);
  
      if(strcmp(gnam, sprintf('mySource/c%d',chan)) ~= 1) 
        src.CloseRBNBConnection(0,0);
        snk.CloseRBNBConnection;
        error('FAIL: Channel name mismatch');
      end
      if(size(gdat) ~= 10)
        src.CloseRBNBConnection(0,0);
        snk.CloseRBNBConnection;
        error('FAIL: Fetched wrong amount of data');
      end
      for idx1=1:10
        time(idx1)=idx+(idx1 - 1)/10.;
        data(idx1)=idx+((idx1 - 1)*2 + chan)/20.;
      end
      if (gtim ~= time)
        src.CloseRBNBConnection(0,0);
        snk.CloseRBNBConnection;
        error('FAIL: Channel time mismatch');
      end
      if (gdat ~= data)
        src.CloseRBNBConnection(0,0);
        snk.CloseRBNBConnection;
        error('FAIL: Channel data mismatch');
      end
    end
  end

  src.CloseRBNBConnection;
  snk.CloseRBNBConnection;

  fprintf('PASS: streaming from oldest test\n');
return
