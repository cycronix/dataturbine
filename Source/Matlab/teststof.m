% V2.2
% rbnb time-based streaming from oldest with a frame that has point times per
% channel.  Other frames will have points times per frame (shared across
% channels).

function [status] = teststof(host)

  if (nargin < 1)
     host = 'localhost';
  end

  % We'll start by putting in one normal frame with shared points times across
  % the channels.
  src = rbnb_source(host,'mySource',10,'none',0);
  cmapsrc = com.rbnb.sapi.ChannelMap;
  cmapsrc.Add('c1');
  cmapsrc.Add('c2');
  cmapsrc.Add('c3');
  times = [ 1 2 3 4 5 6 7 8 9 10 11 12 13 ];
  for idx1=0:2
    cmapsrc.PutTimes(times);
    cmapsrc.PutDataAsFloat64(idx1,times/(idx1 + 1));
  end
  src.Flush(cmapsrc,true);

  % We'll get the sink going now.  We'll stream on the middle channel.  We'll
  % ask for more data than will be in any frame so that we always keep up.
  snk = rbnb_sink(host,'mySink');
  cmapsnk = com.rbnb.sapi.ChannelMap;
  cmapsnk.Add(strcat(char(src.GetClientName),'/c2'));
  snk.Subscribe(cmapsnk,'oldest',60.);

  % We should get back that first frame.
  cmaprslt = snk.Fetch(2000);
  if (cmaprslt.GetIfFetchTimedOut)
    snk.CloseRBNBConnection;
    src.CloseRBNBConnection;
    error('FAIL: Timed out looking for initial frame.');
  end
  if (cmaprslt.NumberOfChannels ~= 1)
    snk.CloseRBNBConnection;
    src.CloseRBNBConnection;
    error('FAIL: Got wrong number of channels in initial frame.');
  end
  rtimes = cmaprslt.GetTimes(0);
  rdata = cmaprslt.GetDataAsFloat64(0);
  if (size(rtimes,1) ~= 13)
    snk.CloseRBNBConnection;
    src.CloseRBNBConnection;
    error('FAIL: Got wrong amount of data in initial frame.');
  end
  if (rdata ~= rtimes/2)
    snk.CloseRBNBConnection;
    src.CloseRBNBConnection;
    error('FAIL: Got wrong data in initial frame.');
  end

  % We'll put in a couple more frames and fill out the frameset plus one extra
  % (with 10 frames in the cache, the frameset should be three frames long).
  % We should immediately get each one.
  for idx=1:3
    times = times + 13;
    for idx1=0:2
      cmapsrc.PutTimes(times);
      cmapsrc.PutDataAsFloat64(idx1,times/(idx1 + 1));
    end
    src.Flush(cmapsrc,true);

    cmaprslt = snk.Fetch(2000);
    if (cmaprslt.GetIfFetchTimedOut)
      snk.CloseRBNBConnection;
      src.CloseRBNBConnection;
      error('FAIL: Timed out looking for a frame.');
    end
    if (cmaprslt.NumberOfChannels ~= 1)
      snk.CloseRBNBConnection;
      src.CloseRBNBConnection;
      error('FAIL: Got wrong number of channels in initial frame.');
    end
    rtimes = cmaprslt.GetTimes(0);
    rdata = cmaprslt.GetDataAsFloat64(0);
    if (size(rtimes,1) ~= 13)
      snk.CloseRBNBConnection;
      src.CloseRBNBConnection;
      error('FAIL: Got wrong amount of data in a frame.');
    end
    if (rdata ~= rtimes/2)
      snk.CloseRBNBConnection;
      src.CloseRBNBConnection;
      error('FAIL: Got wrong data in a frame.');
    end
  end

  % OK, now we'll put in the odd frame.
  btimes = times + 13;
  timesOdd = btimes;
  timesOdd(3) = timesOdd(3) + .5;
  for idx1=0:2
    if (idx1 == 1)
      times = timesOdd;
    else
      times = btimes;
    end
    cmapsrc.PutTimes(times);
    cmapsrc.PutDataAsFloat64(idx1,times/(idx1 + 1));
  end
  src.Flush(cmapsrc,true);

  cmaprslt = snk.Fetch(2000);
  failure = false;
  if (cmaprslt.GetIfFetchTimedOut)
    fprintf('WARNING: failed to get odd frame.\n');
    failure = true;
  else
    if (cmaprslt.NumberOfChannels ~= 1)
      snk.CloseRBNBConnection;
      src.CloseRBNBConnection;
      error('FAIL: Got wrong number of channels in odd frame.');
    end
    rtimes = cmaprslt.GetTimes(0);
    rdata = cmaprslt.GetDataAsFloat64(0);
    if (size(rtimes,1) ~= 13)
      snk.CloseRBNBConnection;
      src.CloseRBNBConnection;
      error('FAIL: Got wrong amount of data in odd frame.');
    end
    if (rtimes(3,1) ~= timesOdd(3))
      snk.CloseRBNBConnection;
      src.CloseRBNBConnection;
      error('FAIL: Got wrong time in odd frame.');
    end
    if (rdata ~= rtimes/2)
      snk.CloseRBNBConnection;
      src.CloseRBNBConnection;
      error('FAIL: Got wrong data in odd frame.');
    end
  end

  % We'll put in several more frames and fill out the frameset and another one
  % (with 10 frames in the cache, the frameset should be three frames long).
  % We should immediately get each one.
  for idx=1:4
    times = times + 13;
    for idx1=0:2
      cmapsrc.PutTimes(times);
      cmapsrc.PutDataAsFloat64(idx1,times/(idx1 + 1));
    end
    src.Flush(cmapsrc,true);

    cmaprslt = snk.Fetch(2000);
    if (cmaprslt.GetIfFetchTimedOut)
      fprintf('WARNING: failed to get frame %d after odd frame.\n',idx);
      failure = true;
    else
      if (cmaprslt.NumberOfChannels ~= 1)
        snk.CloseRBNBConnection;
        src.CloseRBNBConnection;
        error('FAIL: Got wrong number of channels in frame after the odd one.');
      end
      rtimes = cmaprslt.GetTimes(0);
      rdata = cmaprslt.GetDataAsFloat64(0);
      if (size(rtimes,1) ~= 13)
        snk.CloseRBNBConnection;
        src.CloseRBNBConnection;
        error('FAIL: Got wrong amount of data in a frame after the odd one.');
      end
      if (rdata ~= rtimes/2)
        snk.CloseRBNBConnection;
        src.CloseRBNBConnection;
        error('FAIL: Got wrong data in a frame after the odd one.');
      end
    end
  end

  snk.CloseRBNBConnection;
  src.CloseRBNBConnection;

  if (failure)
    error('FAIL: Failed to get some frames.');
  else
    fprintf('PASS: streaming from oldest (time) with an odd frame test\n');
  end
return
