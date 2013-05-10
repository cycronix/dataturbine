% V2.2
% rbnb real-time subscription test

function [status] = testtimesub(host,nframes,fpoints,nduration,reference)

    if (nargin < 1)
	host = 'localhost';
    end
    if (nargin < 2)
	nframes = 100;
    end
    if (nargin < 3)
	fpoints = 10;
    end
    if (nargin < 4)
	nduration = 5.;
    end
    if (nargin < 5)
	reference = 'oldest';
    end

    src = rbnb_source(host,'mySource',nframes,'none',0);
    cmapsrc = com.rbnb.sapi.ChannelMap;
    cmapsrc.Add('c1');

    if (strcmp(reference,'oldest'))
	sframes = floor(nframes/2);
	dynamic = false;
    else
	sframes = 1;
	dynamic = true;
    end

    for idx=1:sframes
	times = ((idx - 1)*fpoints + 1):(idx*fpoints);
	points = -times;
	cmapsrc.PutTimes(times);
	cmapsrc.PutDataAsFloat64(0,points);
	src.Flush(cmapsrc,true);
    end

    snk = rbnb_sink(host,'mySink');
    cmapsnk = com.rbnb.sapi.ChannelMap;
    cmapsnk.Add(strcat(char(src.GetClientName),'/c1'));
    snk.Subscribe(cmapsnk,reference,nduration);
    resmap = snk.Fetch(max(10000,2000));

    lpoint = 0;
    if (~dynamic)
	while (~resmap.GetIfFetchTimedOut)
	    times = resmap.GetTimes(0);
	    data = resmap.GetDataAsFloat64(0);
	    etimes = (lpoint + 1):(lpoint + length(times));
	    edata = -etimes;
	    if (times ~= etimes')
		src.CloseRBNBConnection(false,false);
		snk.CloseRBNBConnection;
		error('FAIL: Fetched times that do not match the expected times');
	    end
	    if (data ~= edata')
		src.CloseRBNBConnection(false,false);
		snk.CloseRBNBConnection;
		error('FAIL: Fetched data that do not match the expected values');
	    end
	    lpoint = lpoint + length(times);
	    if (lpoint >= (sframes - 1)*fpoints)
		break;
	    end
	    resmap = snk.Fetch(2000);
	end
    end

    for idx=(sframes + 1):nframes
	times = ((idx - 1)*fpoints + 1):(idx*fpoints);
	points = -times;
	cmapsrc.PutTimes(times);
	cmapsrc.PutDataAsFloat64(0,points);
	src.Flush(cmapsrc,true);
        pause(0.01);  % mjm cluge:  sync doesnt sync quite well enough for fast puters?
	resmap = snk.Fetch(2000);
	if (resmap.GetIfFetchTimedOut)
	    continue;
	end
	times = resmap.GetTimes(0);
	data = resmap.GetDataAsFloat64(0);
	etimes = (lpoint + 1):(lpoint + length(times));
	edata = -etimes;
	if (times ~= etimes')
	    src.CloseRBNBConnection(false,false);
	    snk.CloseRBNBConnection;
	    error('FAIL: Fetched times that do not match the expected times');
	end
	if (data ~= edata')
	    src.CloseRBNBConnection(false,false);
	    snk.CloseRBNBConnection;
	    error('FAIL: Fetched data that do not match the expected values');
	end
	lpoint = lpoint + length(times);
    end

    if (lpoint ~= nframes*fpoints)
	resmap = snk.Fetch(60000);
	while (~resmap.GetIfFetchTimedOut)
	    times = resmap.GetTimes(0);
	    data = resmap.GetDataAsFloat64(0);
	    etimes = (lpoint + 1):(lpoint + length(times));
	    edata = -etimes;
	    if (times ~= etimes')
		src.CloseRBNBConnection(false,false);
		snk.CloseRBNBConnection;
		error('FAIL: Fetched times that do not match the expected times');
	    end
	    if (data ~= edata')
		src.CloseRBNBConnection(false,false);
		snk.CloseRBNBConnection;
		error('FAIL: Fetched data that do not match the expected values');
	    end
	    lpoint = lpoint + length(times);
	    if (lpoint == nframes*fpoints)
		break;
	    end
	    resmap = snk.Fetch(60000);
	end
    end

    if (lpoint ~= nframes*fpoints)
	src.CloseRBNBConnection(false,false);
	snk.CloseRBNBConnection;
	error('FAIL: Did not retrieve the expected amount of data');
    end

    src.CloseRBNBConnection(false,false);
    snk.CloseRBNBConnection;

    fprintf('PASS: real-time (time-based) subscribe test\n');
return
