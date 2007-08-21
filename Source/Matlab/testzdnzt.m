% V2.2
% rbnb 0 duration request with non-zero duration data test.
% This only works with releases that support at-or-before 0 duration requests.

function [status] = testzdnzt(host,nframes,interval)

    if (nargin < 1)
	host = 'localhost';
    end

    if (nargin < 2)
	nframes = 100;
    end

    if (nargin < 3)
	interval = .75;
    end

    cframes = nframes/10*2;
    if (cframes < 1)
	cframes = 1;
    end
    aframes = nframes*2;

    src = rbnb_source(host,'mySource',cframes,'create',aframes);
    cmapsrc = com.rbnb.sapi.ChannelMap;
    cmapsrc.Add('c1');

    for idx = 1:nframes/2
	time = (idx - 1);
	points = -time;
	cmapsrc.PutTime(time,1.);
	cmapsrc.PutDataAsFloat64(0,points);
	src.Flush(cmapsrc,true);
	time1 = time + 1.;
    end

    src.CloseRBNBConnection(false,true);
    src = rbnb_source(host,'mySource',cframes,'append',aframes);

    time2 = time1 + 2;
    for idx = (nframes/2 + 1):nframes
        time = time2 + (idx - nframes/2 - 1);
        points = -time;
	cmapsrc.PutTime(time,1.);
	cmapsrc.PutDataAsFloat64(0,points);
	src.Flush(cmapsrc,true);
	etime = time + 1.;
    end

    src.CloseRBNBConnection(false,true);
    src = rbnb_source(host,'mySource',cframes,'append',aframes);

    snk = com.rbnb.sapi.Sink;
    snk.OpenRBNBConnection(host,'mySink');
    cmapsnk = com.rbnb.sapi.ChannelMap;
    cmapsnk.Add(strcat(char(src.GetClientName),'/c1'));

    ntimes = ceil(etime/interval);
 %   for idx=1:ntimes
 idx=9;
	rtime = (idx - 1)*interval;
rtime=9;
	atime = floor(rtime);
        if (atime >= time1)
     	    if (atime < time2)
		atime = time1 - 1;
	    end
        end
	adata = -atime;
	snk.Request(cmapsnk,9,0.,'absolute');
	rmap = snk.Fetch(10000);
	if (rmap.GetIfFetchTimedOut)
	    src.CloseRBNBConnection(false,false);
	    snk.CloseRBNBConnection;
	    error('FAIL: Failed to get data');
	end
	if (rmap.NumberOfChannels ~= 1)
	    src.CloseRBNBConnection(false,false);
	    snk.CloseRBNBConnection;
	    error('FAIL: Failed to get correct channel');
	end
	times = rmap.GetTimes(0);
	data = rmap.GetDataAsFloat64(0);
	if (length(times) ~= 1)
	    src.CloseRBNBConnection(false,false);
	    snk.CloseRBNBConnection;
	    error('FAIL: Wrong amount of data retrieved');
	end
	if (times(1) ~= atime)
	    src.CloseRBNBConnection(false,false);
	    snk.CloseRBNBConnection;
	    error('FAIL: Wrong time retrieved');
	end
	if (data(1) ~= adata)
	    src.CloseRBNBConnection(false,false);
	    snk.CloseRBNBConnection;
	    error('FAIL: Wrong data retrieved');
	end
 %   end

    src.CloseRBNBConnection(false,false);
    snk.CloseRBNBConnection;

    fprintf('PASS: zero duration request non-zero duration data test\n');
return
