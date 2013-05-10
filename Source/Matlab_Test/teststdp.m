% V2.2
% rbnb time-based streaming from oldest with duplicate points.

function [status] = teststdp(host)

  if (nargin < 1)
     host = 'localhost';
  end

  src = rbnb_source(host,'mySource',100,'none',0);
  cmapsrc = com.rbnb.sapi.ChannelMap;
  cmapsrc.Add('c');

  % Initially, we'll put in a single frame with 13 points, the last couple of
  % which are duplicated.
  times = [ 0 1 2 3 4 5 6 7 8 9 10 11 11 ];
  data = -times;
  cmapsrc.PutTimes(times);
  cmapsrc.PutDataAsFloat64(0,data);
  src.Flush(cmapsrc,true);

  % We ask for a duration of 1, which should give us two points at a time until
  % we get to the end, where we'll get three.  Code prior to V2.2 will
  % repeatedly return the last two points after that.  Code that works should
  % timeout.
  snk = rbnb_sink(host,'mySink');
  cmapsnk = com.rbnb.sapi.ChannelMap;
  cmapsnk.Add(strcat(char(src.GetClientName),'/c'));
  snk.Subscribe(cmapsnk,'oldest',1.);

  lastTime = -1;
  repeated = false;
  while ~repeated
    cmaprslt = snk.Fetch(2000);
    if (cmaprslt.GetIfFetchTimedOut)
      if (lastTime ~= 11)
	snk.CloseRBNBConnection;
	src.CloseRBNBConnection;
	error('FAIL: Timed out waiting for data.');
      end
      break;
    end

    if (cmaprslt.NumberOfChannels ~= 1)
      snk.CloseRBNBConnection;
      src.CloseRBNBConnection;
      error('FAIL: Got the wrong number of channels.');
    end

    rtimes = cmaprslt.GetTimes(0);
    rdata = cmaprslt.GetDataAsFloat64(0);
    if (size(rtimes,1) == 2)
      if (lastTime == 11)
        repeated = true;
        fprintf('WARNING: Saw repeated data with one frame.');
      end
      if (rdata ~= -rtimes)
	snk.CloseRBNBConnection;
	src.CloseRBNBConnection;
	error('FAIL: Got wrong data.');
      end
      lastTime = rtimes(2);
    elseif (size(rtimes,1) == 3)
      if (rdata ~= -rtimes)
	snk.CloseRBNBConnection;
	src.CloseRBNBConnection;
	error('FAIL: Got wrong data (a).');
      elseif (rtimes(2) ~= rtimes(3))
	snk.CloseRBNBConnection;
	src.CloseRBNBConnection;
	error('FAIL: Got wrong times (a).');
      elseif (rtimes(3) ~= 11)
	snk.CloseRBNBConnection;
	src.CloseRBNBConnection;
	error('FAIL: Unexpected duplicate times.');
      end
      lastTime = rtimes(3);
    else
      snk.CloseRBNBConnection;
      src.CloseRBNBConnection;
      error('FAIL: Got wrong amount of data.');
    end
  end

  % Add a second frame with three duplicates of the the last point of the
  % previous frame.
  times = [ 11 11 11 ];
  data = -times;
  cmapsrc.PutTimes(times);
  cmapsrc.PutDataAsFloat64(0,data);
  src.Flush(cmapsrc,true);

  % If we didn't get the repeat last time, then this time, we should entirely
  % skip the new frame.
  while ~repeated
    cmaprslt = snk.Fetch(2000);
    if (cmaprslt.GetIfFetchTimedOut)
      if (lastTime ~= 11)
	snk.CloseRBNBConnection;
	src.CloseRBNBConnection;
	error('FAIL: Timed out waiting for data (2).');
      end
      break;
    end

    if (cmaprslt.NumberOfChannels ~= 1)
      snk.CloseRBNBConnection;
      src.CloseRBNBConnection;
      error('FAIL: Got the wrong number of channels (2).');
    end

    rtimes = cmaprslt.GetTimes(0);
    rdata = cmaprslt.GetDataAsFloat64(0);
    if (rtimes(1) == 11)
      repeated = true;
      fprintf('WARNING: Saw repeated data with two frames.');
    else
      snk.CloseRBNBConnection;
      src.CloseRBNBConnection;
      error('FAIL: Got unexpected data (2).');
    end
  end

  % Add a third frame with a duplicate of the last point of the previous
  % frame.
  times = [ 11 12 13 14 15 ];
  data = -times;
  cmapsrc.PutTimes(times);
  cmapsrc.PutDataAsFloat64(0,data);
  src.Flush(cmapsrc,true);

  % If we didn't get the repeat last time, then we should now get the last four
  % points of the new frame in pairs, skipping over the duplicate point in the
  % new frame.
  while ~repeated
    cmaprslt = snk.Fetch(2000);
    if (cmaprslt.GetIfFetchTimedOut)
      if (lastTime ~= 15)
	snk.CloseRBNBConnection;
	src.CloseRBNBConnection;
	error('FAIL: Timed out waiting for data (3).');
      end
      break;
    end

    if (cmaprslt.NumberOfChannels ~= 1)
      snk.CloseRBNBConnection;
      src.CloseRBNBConnection;
      error('FAIL: Got the wrong number of channels (3).');
    end

    rtimes = cmaprslt.GetTimes(0);
    rdata = cmaprslt.GetDataAsFloat64(0);
    if (size(rtimes,1) == 2)
      if (rdata ~= -rtimes)
	snk.CloseRBNBConnection;
	src.CloseRBNBConnection;
	error('FAIL: Got wrong data (3).');
      end
      if ((rtimes(1) == 11) || (rtimes(2) == 11))
	snk.CloseRBNBConnection;
	src.CloseRBNBConnection;
	error('FAIL: Unexpectedly duplicated time.');
      end
      lastTime = rtimes(2);
    else
      snk.CloseRBNBConnection;
      src.CloseRBNBConnection;
      error('FAIL: Got wrong amount of data (3).');
    end
  end
  snk.CloseRBNBConnection;

  % Finally, we should now be able to try to go through the data again.  This
  % time, we should see one entry with seven points in the middle.
  snk.OpenRBNBConnection(host,'mySink');
  snk.Subscribe(cmapsnk,'oldest',1.0);
  repeated1 = repeated;
  repeated = false;
  lastTime = -1;
  while ~repeated
    cmaprslt = snk.Fetch(2000);
    if (cmaprslt.GetIfFetchTimedOut)
      if (lastTime ~= 15)
	snk.CloseRBNBConnection;
	src.CloseRBNBConnection;
	error('FAIL: Timed out waiting for data (4).');
      end
      break;
    end

    if (cmaprslt.NumberOfChannels ~= 1)
      snk.CloseRBNBConnection;
      src.CloseRBNBConnection;
      error('FAIL: Got the wrong number of channels (4).');
    end

    rtimes = cmaprslt.GetTimes(0);
    rdata = cmaprslt.GetDataAsFloat64(0);
    if ((lastTime == 11) && (rtimes(1) == 11))
      repeated = true;
    elseif (size(rtimes,1) == 2)
      if (rdata ~= -rtimes)
	snk.CloseRBNBConnection;
	src.CloseRBNBConnection;
	error('FAIL: Got wrong data (4).');
      end
      lastTime = rtimes(2);
    elseif (size(rtimes,1) == 7)
      if (rdata ~= -rtimes)
	snk.CloseRBNBConnection;
	src.CloseRBNBConnection;
	error('FAIL: Got wrong data (4a).');
      elseif ((rtimes(2) ~= rtimes(3)) || (rtimes(3) ~= rtimes(4)) || (rtimes(4) ~= rtimes(5)) || (rtimes(5) ~= rtimes(6)) ||  (rtimes(6) ~= rtimes(7)))
	snk.CloseRBNBConnection;
	src.CloseRBNBConnection;
	error('FAIL: Got wrong times (4a).');
      elseif (rtimes(7) ~= 11)
	snk.CloseRBNBConnection;
	src.CloseRBNBConnection;
	error('FAIL: Unexpected duplicate times.');
      end
      lastTime = rtimes(7);    else
      snk.CloseRBNBConnection;
      src.CloseRBNBConnection;
      error('FAIL: Got amount of data (4).');
    end
  end
  snk.CloseRBNBConnection;
  src.CloseRBNBConnection;

  if (repeated || repeated1)
    if (repeated && ~repeated1)
       error('FAIL: Got repeated data.');
    else
       error('FAIL: Got repeated data in both tests.');
    end
  end

  fprintf('PASS: streaming from oldest (time) with duplicates test\n');
return
