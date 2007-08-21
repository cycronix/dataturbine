% V2.2
% rbnb test to check out the max wait time request option.

function [status] = testmaxw(host)

    if (nargin < 1)
	host = 'localhost';
    end

    snk = rbnb_sink(host,'mySink');
    cmapsnk = com.rbnb.sapi.ChannelMap;
    cmapsnk.Add('mySource/c');
    ro = com.rbnb.sapi.RequestOptions;
    ro.setMaxWait(5000);

    snk.Request(cmapsnk,4.,0.,'absolute',ro);
    amap = snk.Fetch(2000);
    if amap.GetIfFetchTimedOut
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: pre-source zero duration request timed out');
    end
    if amap.NumberOfChannels ~= 0
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: wrong number of channels from pre-source zero duration request');
    end

    snk.Request(cmapsnk,5.,1.,'absolute',ro);
    amap = snk.Fetch(2000);
    if amap.GetIfFetchTimedOut
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: pre-source duration request timed out');
    end
    if amap.NumberOfChannels ~= 0
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: wrong number of channels from pre-source duration request');
    end

    src = rbnb_source(host,'mySource',100,'none',0);
    cmapsrc = com.rbnb.sapi.ChannelMap;
    cmapsrc.Add('c');

    snk.Request(cmapsnk,4.,0.,'absolute',ro);
    amap = snk.Fetch(2000);
    if amap.GetIfFetchTimedOut
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: pre-flush zero duration request timed out');
    end
    if amap.NumberOfChannels ~= 0
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: wrong number of channels from pre-flush zero duration request');
    end

    snk.Request(cmapsnk,5.,1.,'absolute',ro);
    amap = snk.Fetch(2000);
    if amap.GetIfFetchTimedOut
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: pre-flush duration request timed out');
    end
    if amap.NumberOfChannels ~= 0
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: wrong number of channels from pre-flush duration request');
    end

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

    ro.setMaxWait(1500);
    snk.Request(cmapsnk,4.,0.,'absolute',ro);
    amap = snk.Fetch(3000);
    if amap.NumberOfChannels ~= 1
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: wrong number of channels from zero duration request (1)');
    end
    atime = amap.GetTimes(0);
    adata = amap.GetDataAsFloat64(0);
    if atime ~= fulltime(30:30)
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: wrong time from zero duration request (1)');
    end
    if adata ~= fulldata(30:30)
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: wrong data from zero duration request (1)');
    end

    snk.Request(cmapsnk,5.,1.,'absolute',ro);
    amap = snk.Fetch(3000);
    if amap.NumberOfChannels ~= 0
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: wrong number of channels from duration request (1)');
    end

    ro.setExtendStart(true);
    snk.Request(cmapsnk,5.95,1.,'absolute',ro);
    amap = snk.Fetch(3000);
    if amap.NumberOfChannels ~= 1
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: wrong number of channels from extend start request (1)');
    end
    atime = amap.GetTimes(0);
    adata = amap.GetDataAsFloat64(0);
    if atime ~= fulltime(30:30)
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: wrong time from extend start request (1)');
    end
    if adata ~= fulldata(30:30)
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: wrong data from extend start request (1)');
    end

    for j=0:9
	time(j+1) = 4 + j/10.;
	data(j+1) = 40 + j;
    end
    fulltime = [ fulltime time ];
    fulldata = [ fulldata data ];
    cmapsrc.PutTimes(time);
    cmapsrc.PutDataAsFloat64(0,data);
    ro.setExtendStart(false);
    snk.Request(cmapsnk,4.,0.,'absolute',ro);
    pause(0.5);
    src.Flush(cmapsrc,true);
    amap = snk.Fetch(3000);
    if amap.NumberOfChannels ~= 1
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: wrong number of channels from zero duration request (2)');
    end
    atime = amap.GetTimes(0);
    adata = amap.GetDataAsFloat64(0);
    if atime ~= fulltime(31:31)
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: wrong time from zero duration request (2)');
    end
    if adata ~= fulldata(31:31)
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: wrong data from zero duration request (2)');
    end

    for j=0:9
	time(j+1) = 5 + j/10.;
	data(j+1) = 50 + j;
    end
    fulltime = [ fulltime time ];
    fulldata = [ fulldata data ];
    cmapsrc.PutTimes(time);
    cmapsrc.PutDataAsFloat64(0,data);
    ro.setExtendStart(false);
    snk.Request(cmapsnk,5.,1.,'absolute',ro);
    pause(0.5);
    src.Flush(cmapsrc,true);
    amap = snk.Fetch(3000);
    if amap.NumberOfChannels ~= 1
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: wrong number of channels from duration request (2)');
    end
    atime = amap.GetTimes(0);
    adata = amap.GetDataAsFloat64(0);
    if atime' ~= fulltime(41:50)
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: wrong time from duration request (2)');
    end
    if adata' ~= fulldata(41:50)
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: wrong data from duration request (2)');
    end

    for j=0:9
	time(j+1) = 6 + j/10.;
	data(j+1) = 60 + j;
    end
    fulltime = [ fulltime time ];
    fulldata = [ fulldata data ];
    cmapsrc.PutTimes(time);
    cmapsrc.PutDataAsFloat64(0,data);
    ro.setExtendStart(true);
    snk.Request(cmapsnk,5.95,1.,'absolute',ro);
    pause(0.5);
    src.Flush(cmapsrc,true);
    amap = snk.Fetch(3000);
    if amap.NumberOfChannels ~= 1
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: wrong number of channels from extend start request (2)');
    end
    atime = amap.GetTimes(0);
    adata = amap.GetDataAsFloat64(0);
    if atime' ~= fulltime(50:60)
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: wrong time from extend start request (2)');
    end
    if adata' ~= fulldata(50:60)
	src.CloseRBNBConnection(false,false);
	src.CloseRBNBConnection;
	error('FAIL: wrong data from extend start request (2)');
    end

    src.CloseRBNBConnection(false,false);
    snk.CloseRBNBConnection;
    fprintf('PASS: max wait test\n');
return
