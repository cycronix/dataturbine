% V2.2
% rbnb extend start request test.
% This only works with releases that support extend start requests.

function [status] = testextst(host)

    if (nargin < 1)
	host = 'localhost';
    end

% Zero duration data.

    src = rbnb_source(host,'mySource',100,'none',0);
    cmapsrc = com.rbnb.sapi.ChannelMap;
    cmapsrc.Add('c');

    for i=1:3
	for j=0:9
	    time(j+1) = i + j/10.;
	    data(j+1) = i*10 + j;
	end
	if i ~= 1
	    fulltime = [ fulltime time ];
	    fulldata = [ fulldata data ];
	else
	    fulltime = time;
	    fulldata = data;
	end
	cmapsrc.PutTimes(time);
	cmapsrc.PutDataAsFloat64(0,data);
	src.Flush(cmapsrc,true);
    end
    for i=5:7
	for j=0:9
	    time(j+1) = i + j/10.;
	    data(j+1) = i*10 + j;
	end
	fulltime = [ fulltime time ];
	fulldata = [ fulldata data ];
	cmapsrc.PutTimes(time);
	cmapsrc.PutDataAsFloat64(0,data);
	src.Flush(cmapsrc,true);
    end
    for i=9:12
	for j=0:9
	    time(j+1) = i + j/10.;
	    data(j+1) = i*10 + j;
	end
	fulltime = [ fulltime time ];
	fulldata = [ fulldata data ];
	cmapsrc.PutTimes(time);
	cmapsrc.PutDataAsFloat64(0,data);
	src.Flush(cmapsrc,true);
    end
    
    snk = rbnb_sink(host,'mySink');
    cmapsnk = com.rbnb.sapi.ChannelMap;
    cmapsnk.Add('mySource/c');

    snk.Request(cmapsnk,8.,1.5,'absolute');
    amap = snk.Fetch(10000);
    if amap.NumberOfChannels ~= 1
	src.CloseRBNBConnection;
	snk.CloseRBNBConnection;
	error('FAIL: wrong number of channels retrieved (1)');
    end
    times = amap.GetTimes(0);
    adata = amap.GetDataAsFloat64(0);
    if length(times) ~= 6
	src.CloseRBNBConnection;
	snk.CloseRBNBConnection;
	error('FAIL: wrong amount of data retrieved (1)');
    end
    if times' ~= fulltime(61:66)
	src.CloseRBNBConnection;
	snk.CloseRBNBConnection;
	error('FAIL: wrong times retrieved (1)');
    end
    if adata' ~= fulldata(61:66)
	src.CloseRBNBConnection;
	snk.CloseRBNBConnection;
	error('FAIL: wrong data retrieved (1)');
    end

    ro = com.rbnb.sapi.RequestOptions;
    ro.setExtendStart(true);

    snk.Request(cmapsnk,8.,1.5,'absolute',ro);
    amap = snk.Fetch(10000);
    if amap.NumberOfChannels ~= 1
	src.CloseRBNBConnection;
	snk.CloseRBNBConnection;
	error('FAIL: wrong number of channels retrieved (2)');
    end
    times = amap.GetTimes(0);
    adata = amap.GetDataAsFloat64(0);
    if length(times) ~= 7
	src.CloseRBNBConnection;
	snk.CloseRBNBConnection;
	error('FAIL: wrong amount of data retrieved (2)');
    end
    if times' ~= fulltime(60:66)
	src.CloseRBNBConnection;
	snk.CloseRBNBConnection;
	error('FAIL: wrong times retrieved (2)');
    end
    if adata' ~= fulldata(60:66)
	src.CloseRBNBConnection;
	snk.CloseRBNBConnection;
	error('FAIL: wrong data retrieved (2)');
    end
    
    src.CloseRBNBConnection;
    snk.CloseRBNBConnection;

% Non-zero duration data.

    src = rbnb_source(host,'mySource',100,'none',0);
    cmapsrc = com.rbnb.sapi.ChannelMap;
    cmapsrc.Add('c');

    for i=1:3
	for j=0:9
	    time(j+1) = i + j/10.;
	    data(j+1) = i*10 + j;
	end
	if i ~= 1
	    fulltime = [ fulltime time ];
	    fulldata = [ fulldata data ];
	else
	    fulltime = time;
	    fulldata = data;
	end
	cmapsrc.PutTime(i,1.);
	src.Flush(cmapsrc,true);
    end
    for i=5:7
	for j=0:9
	    time(j+1) = i + j/10.;
	    data(j+1) = i*10 + j;
	end
	fulltime = [ fulltime time ];
	fulldata = [ fulldata data ];
	cmapsrc.PutTime(i,1.);
	cmapsrc.PutDataAsFloat64(0,data);
	src.Flush(cmapsrc,true);
    end
    for i=9:12
	for j=0:9
	    time(j+1) = i + j/10.;
	    data(j+1) = i*10 + j;
	end
	fulltime = [ fulltime time ];
	fulldata = [ fulldata data ];
	cmapsrc.PutTime(i,1.);
	cmapsrc.PutDataAsFloat64(0,data);
	src.Flush(cmapsrc,true);
    end
    
    snk = rbnb_sink(host,'mySink');
    cmapsnk = com.rbnb.sapi.ChannelMap;
    cmapsnk.Add('mySource/c');

    snk.Request(cmapsnk,8.,1.5,'absolute');
    amap = snk.Fetch(10000);
    if amap.NumberOfChannels ~= 1
	src.CloseRBNBConnection;
	snk.CloseRBNBConnection;
	error('FAIL: wrong number of channels retrieved (3)');
    end
    times = amap.GetTimes(0);
    adata = amap.GetDataAsFloat64(0);
    if length(times) ~= 5
	src.CloseRBNBConnection;
	snk.CloseRBNBConnection;
	error('FAIL: wrong amount of data retrieved (3)');
    end
    if times' ~= fulltime(61:65)
	src.CloseRBNBConnection;
	snk.CloseRBNBConnection;
	error('FAIL: wrong times retrieved (3)');
    end
    if adata' ~= fulldata(61:65)
	src.CloseRBNBConnection;
	snk.CloseRBNBConnection;
	error('FAIL: wrong data retrieved (3)');
    end

    ro = com.rbnb.sapi.RequestOptions;
    ro.setExtendStart(true);

    snk.Request(cmapsnk,8.,1.5,'absolute',ro);
    amap = snk.Fetch(10000);
    if amap.NumberOfChannels ~= 1
	src.CloseRBNBConnection;
	snk.CloseRBNBConnection;
	error('FAIL: wrong number of channels retrieved (4)');
    end
    times = amap.GetTimes(0);
    adata = amap.GetDataAsFloat64(0);
    if length(times) ~= 6
	src.CloseRBNBConnection;
	snk.CloseRBNBConnection;
	error('FAIL: wrong amount of data retrieved (4)');
    end
    if times' ~= fulltime(60:65)
	src.CloseRBNBConnection;
	snk.CloseRBNBConnection;
	error('FAIL: wrong times retrieved (4)');
    end
    if adata' ~= fulldata(60:65)
	src.CloseRBNBConnection;
	snk.CloseRBNBConnection;
	error('FAIL: wrong data retrieved (4)');
    end
    
    src.CloseRBNBConnection;
    snk.CloseRBNBConnection;

    fprintf('PASS: extend start request test\n');
return
