% testreg.m
% Script to test source registration of empty channels, and streaming.
%
% Problem description:
% create a source, register the channels, but don't put in any data.
% Then subscribe by time from oldest or newest, and the first fetch will
%  cause the problem.
%


%
% 2005/07/06  WHF  Created.
%

clear import
import com.rbnb.sapi.*

source = Source;
source.OpenRBNBConnection('localhost:3333', 'testreg_source');

sink = Sink;
sink.OpenRBNBConnection;
java.lang.System.setProperty('com.rbnb.sapi.ChannelMap.debug','true');
cmap = ChannelMap;
cmap.Add('foo/bar');
%cmap.PutDataAsString(0, 'my string');
%cmap.PutUserInfo(0, 'my user info');
source.Register(cmap);
cmap = ChannelMap;
cmap.Add('testreg_source/foo/bar');
start = 1;
duration = 1;
sink.Subscribe(cmap, start, duration, 'newest');
cmap = sink.Fetch(1000);
disp('Result:')
cmap
timeout = cmap.GetIfFetchTimedOut

sink.CloseRBNBConnection
source.CloseRBNBConnection
