% V2.4.2
% rbnb multiple ring buffer source test.

function [status] = testmrb(host)

  if (nargin < 1)
    host = 'localhost';
  end

  % We'll create a multiple ring buffer source by putting in several channels,
  % one at a time.  The data times are such that this should fail for V2.0x
  % servers.
  src = rbnb_source(host,'mySource',10,'none',0);

  cmapsra = com.rbnb.sapi.ChannelMap;
  cmapsra.Add('a');
  cmapsra.PutTime(0.,1.);
  cmapsra.PutDataAsFloat32(0,0:10);
  src.Flush(cmapsra,true);
  cmapsra.PutTime(1.,1.);
  cmapsra.PutDataAsFloat32(0,11:20);
  src.Flush(cmapsra,true);

  cmapsrb = com.rbnb.sapi.ChannelMap;
  cmapsrb.Add('b');
  cmapsrb.PutTime(0.,1.);
  cmapsrb.PutDataAsFloat32(0,100:110);
  src.Flush(cmapsrb,true);
  cmapsrb.PutTime(2.,1.);
  cmapsrb.PutDataAsFloat32(0,121:130);
  src.Flush(cmapsrb,true);

  cmapsrc = com.rbnb.sapi.ChannelMap;
  cmapsrc.Add('c');
  cmapsrc.PutTime(1.,1.);
  cmapsrc.PutDataAsFloat32(0,211:220);
  src.Flush(cmapsrc,true);
  cmapsrc.PutTime(2.,1.);
  cmapsrc.PutDataAsFloat32(0,221:230);
  src.Flush(cmapsrc,true);

  % Create a sink and try getting various combinations of channels.
  snk = rbnb_sink(host,'mySink');

  % Request everything.
  cmapsnk1 = com.rbnb.sapi.ChannelMap;
  cmapsnk1.Add(strcat(char(src.GetClientName),'/...'));
  snk.Request(cmapsnk1,0.,3.,'absolute');
  cmaprsp = snk.Fetch(2000);
  
  if (cmaprsp.GetIfFetchTimedOut)
    snk.CloseRBNBConnection;
    src.CloseRBNBConnection;
    error('FAIL: Timed out requesting all data');
  end
  if (cmaprsp.NumberOfChannels ~= 3)
    snk.CloseRBNBConnection;
    src.CloseRBNBConnection;
    error('FAIL: Got wrong number of channels when requesting all data');
  end
  rtimes0 = cmaprsp.GetTimes(0);
  rtimes1 = cmaprsp.GetTimes(1);
  rtimes2 = cmaprsp.GetTimes(2);
  if ((size(rtimes0,1) ~= 21) || (size(rtimes1,1) ~= 21) || (size(rtimes2,1) ~= 20))
    snk.CloseRBNBConnection;
    src.CloseRBNBConnection;
    error('FAIL: Got wrong amount of data when requesting all data');
  end

  % Try getting just the middle part of the second channel.  This will fail
  % with post-V2.1 and pre-V2.4.2 servers due to a string index out of range
  % bug in the server.
  cmapsnk2 = com.rbnb.sapi.ChannelMap;
  cmapsnk2.Add(strcat(char(src.GetClientName),'/b'));
  snk.Request(cmapsnk2,.5,2.,'absolute');
  cmaprsp = snk.Fetch(2000);
  if (cmaprsp.GetIfFetchTimedOut)
    snk.CloseRBNBConnection;
    src.CloseRBNBConnection;
    error('FAIL: Timed out requesting middle of second channel');
  end
  if (cmaprsp.NumberOfChannels ~= 1)
    snk.CloseRBNBConnection;
    src.CloseRBNBConnection;
    error('FAIL: Got wrong number of channels when requesting middle of second channel');
  end
  rtimes1 = cmaprsp.GetTimes(0);
  if (size(rtimes1,1) ~= 10)
    snk.CloseRBNBConnection;
    src.CloseRBNBConnection;
    error('FAIL: Got wrong amount of data when requesting middle of second channel');
  end
  rdata1 = cmaprsp.GetDataAsFloat32(0);
  adata = [ 106, 107, 108, 109, 110, 121, 122, 123, 124, 125 ]';
  if (rdata1 ~= adata)
    snk.CloseRBNBConnection;
    src.CloseRBNBConnection;
    error('FAIL: Got wrong data when requesting middle of second channel');
  end

  % Try getting the last part of the first and third channels.  This would also
  % have problems with post-V2.1 and pre-V2.4.2 servers, but we should never
  % get here then.
  cmapsnk3 = com.rbnb.sapi.ChannelMap;
  cmapsnk3.Add(strcat(char(src.GetClientName),'/a'));
  cmapsnk3.Add(strcat(char(src.GetClientName),'/c'));
  snk.Request(cmapsnk3,0.,1.,'newest');
  cmaprsp = snk.Fetch(2000);
  if (cmaprsp.GetIfFetchTimedOut)
    snk.CloseRBNBConnection;
    src.CloseRBNBConnection;
    error('FAIL: Timed out requesting end of first and last channels.');
  end
  if (cmaprsp.NumberOfChannels ~= 2)
    snk.CloseRBNBConnection;
    src.CloseRBNBConnection;
    error('FAIL: Got wrong number of channels when requesting end of first and last channels');
  end
  rtimes0 = cmaprsp.GetTimes(0);
  rtimes2 = cmaprsp.GetTimes(1);
  if (rtimes0 + 1 ~= rtimes2)
    snk.CloseRBNBConnection;
    src.CloseRBNBConnection;
    error('FAIL: Got wrong times when requesting end of first and last channels');
  end
  rdata2 = cmaprsp.GetDataAsFloat32(1);
  adata = [ 221, 222, 223, 224, 225, 226, 227, 228, 229, 230 ]';
  if (rdata2 ~= adata)
    snk.CloseRBNBConnection;
    src.CloseRBNBConnection;
    error('FAIL: Got wrong data when requesting end of first and last channels');
  end

  snk.CloseRBNBConnection;
  src.CloseRBNBConnection;

  fprintf('PASS: multiple ring buffer test\n');
return


