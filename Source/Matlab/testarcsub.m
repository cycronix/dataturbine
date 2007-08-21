% V2.2
% rbnb archive subscription test.
% This only works with releases that support time-based subscriptions.

function [status] = testarcsub(host,nframes,fpoints,nduration)

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

    src = rbnb_source(host,'mySource',100,'create',nframes);
    cmapsrc = com.rbnb.sapi.ChannelMap;
    cmapsrc.Add('c1');

    for idx=1:nframes
	times = ((idx - 1)*fpoints + 1):(idx*fpoints);
	points = -times;
	cmapsrc.PutTimes(times);
	cmapsrc.PutDataAsFloat64(0,points);
	src.Flush(cmapsrc,true);
    end

    src.CloseRBNBConnection(false,true);
    src = rbnb_source(host,'mySource',100,'append',nframes);

    snk = rbnb_sink(host,'mySink');
    cmapsnk = com.rbnb.sapi.ChannelMap;
    cmapsnk.Add(strcat(char(src.GetClientName),'/c1'));
    snk.Subscribe(cmapsnk,'oldest',nduration);
    resmap = snk.Fetch(max(10000,nduration*1000 + 1000));

    lpoint = 0;
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
	resmap = snk.Fetch(max(10000,nduration*1000 + 1000));
    end

    if (lpoint ~= nframes*fpoints)
	src.CloseRBNBConnection(false,false);
	snk.CloseRBNBConnection;
	error('FAIL: Did not retrieve the expected amount of data');
    end

    src.CloseRBNBConnection(false,false);
    snk.CloseRBNBConnection;

    fprintf('PASS: subscribe from archive test\n');
return

