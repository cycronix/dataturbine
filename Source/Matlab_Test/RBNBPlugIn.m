%RBNBPlugIn receives requests from RBNB, calls m-files to handle them
% note blocks until ctrl-C received
% call as RBNBPlugIn(rbnb,piname)
% example: RBNBPlugIn('localhost:3333','matlab')
function RBNBPlugIn(rbnb,piname)

timeout=2000; %max wait for data from sink

import com.rbnb.sapi.*;

if nargin~=2
    error('try calling RBNBPlugIn(rbnb,piname)');
end
   
%set up plugin and sink
pi=PlugIn;
pi.OpenRBNBConnection(rbnb,piname);
snk=Sink;
snk.OpenRBNBConnection(rbnb,'matsink');

%loop handling requests
try,
while(1)
	drawnow             % so matlab will update figures, etc.
	picm=pi.Fetch(100); %time out so ctrl-C will terminate
	if (picm.GetIfFetchTimedOut), continue; end
	if picm.GetRequestReference.equalsIgnoreCase('registration')
		%if wildcard registration request, give null response
		if (picm.GetName(0).endsWith('*')||picm.GetName(0).endsWith('...'))
			picm.Clear;
		else
			picm.PutTime(java.lang.System.currentTimeMillis/1000.0,0);
			for i=1:picm.NumberOfChannels
				picm.PutDataAsByteArray(i-1,[0]);
			end
		end
		pi.Flush(picm);
	else
		picm
		cm=ChannelMap;
		%clear vars times;
        vars = {};
        times = {};
		opts = java.util.Properties;
		outChan = 0;
		for ii = 0:picm.NumberOfChannels-1,
			if picm.GetName(ii).length == 0,
				opts = parseOptions(picm.GetDataAsString(ii));
				outChan = 1;
				continue;
            end
		
            if 0, % EMF's original channel extraction code.  
                % It parses requests such as a/b/c/d into:
                %  a = plotting MATLAB function
                %  b = RBNB sourcename
                %  c,d, etc = channels under b.
                % Although neat, this is inconsistent with everything else
                %  in RBNB.
                %extract input src/chans, build request map
                chans=picm.GetName(ii).split('/');
                userfunc=chans(1); %matlab function to call
                source=chans(2); %input channel's RBNB source name
                for i=3:length(chans)
                    %other entries are either channel names or constants
                    x=str2double(chans(i));
                    if (isnan(x))
                        vars{i-2}=source.concat('/').concat(chans(i));
                        cm.Add(vars{i-2});
                    else
                        vars{i-2}=x;
                    end
                end
            else, % new WHF parsing logic
                chan = picm.GetName(ii);
                slashI = chan.indexOf('/');
                if slashI ~= -1,
                    userfunc = chan.substring(0, slashI);
                    vars{end+1} = chan.substring(slashI+1);
                    cm.Add(vars{end});
                end
            end
		end % for ii
		if (cm.NumberOfChannels>0)
			%fetch input data
			snk.Request(cm,picm.GetRequestStart,picm.GetRequestDuration,picm.GetRequestReference);
			cmr=snk.Fetch(timeout);
			snk.RequestRegistration(cm);
			cmreg = snk.Fetch(timeout);
		else
			cmr=ChannelMap;
		end
		if (~cmr.GetIfFetchTimedOut && cm.NumberOfChannels==cmr.NumberOfChannels)
			dosend=1;
			%prepare input data for matlab function
			for i=1:length(vars)
				if (~isnumeric(vars{i}))
					idx=cmr.GetIndex(vars{i});
					if (idx>-1)
						times{i} = cmr.GetTimes(idx);
						
						% Insert some properties into opts, based on acquired
						%  meta-data.  If the user has already specified these
						%  properties, do not overwrite.
						if (~opts.containsKey('title')),
							opts.put('title', cmr.GetName(idx));
						end
						regIdx = cmreg.GetIndex(cmr.GetName(idx));
						if (regIdx >= 0 && ~opts.containsKey('ylabel')),
                            % WHF  GetUser had trouble with duplicated
                            %  user tags in Altair data:
							% opts.put('ylabel', cmreg.GetUserInfo(regIdx));
                            info = cmreg.GetDataAsString(regIdx);
                            userTagIndex = info(1).indexOf('<user>');
                            closeIndex = info(1).indexOf('</user>');
                            info = info(1).substring( ...
                                userTagIndex+length('<user>'), closeIndex);
                            opts.put('ylabel', info);
						end
						if (cmr.GetType(idx)==ChannelMap.TYPE_FLOAT32) vars{i}=cmr.GetDataAsFloat32(idx);
						elseif (cmr.GetType(idx)==ChannelMap.TYPE_FLOAT64) vars{i}=cmr.GetDataAsFloat64(idx);
						elseif (cmr.GetType(idx)==ChannelMap.TYPE_INT32) vars{i}=cmr.GetDataAsInt32(idx);
						elseif (cmr.GetType(idx)==ChannelMap.TYPE_INT64) vars{i}=cmr.GetDataAsInt64(idx);
						else
							disp('data type not float32, float64, int32, or int64, cannot process');
							dosend=0;
						end
					else
						disp('failed to obtain input data, aborting');
						dosend=0;
					end
				end
			end
			if (dosend)
				%call userfunc, flush data - assumes same number of
				%points as input channels
				try
					y=eval(strcat(char(userfunc),'(times, vars, opts)')); %must return figure handle
					if 0,
					print(y,'-r0','-djpeg','PItemp'); %write jpg file
					close(y);
					fid=fopen('PItemp.jpg');
					A=int8(fread(fid,'int8')); %read jpg file
					fclose(fid);
					delete PItemp.jpg
					else
						print(y,'-r0','-dpng','PItemp'); 
						close(y);
						fid=fopen('PItemp.png');
						A=int8(fread(fid,'int8')); %read jpg file
						fclose(fid);
						delete PItemp.png							
					end
					picm.PutTime(picm.GetRequestStart,picm.GetRequestDuration);
					picm.PutDataAsByteArray(outChan, A);
				catch
					disp(strcat('error calling ',char(userfunc),'; aborting'));
					foo=lasterror;
					disp(foo.message);
				end
			end
		end
		pi.Flush(picm);
	end
end
catch,
	le = lasterror;
	snk.CloseRBNBConnection;
	pi.CloseRBNBConnection;
	rethrow(le);
end

function out = parseOptions(optArray)
% PARSEOTPIONS  Internal function to handle plug-in options.
%
% Accepts a String[], where each string is of the form, key=value.  Returns
%  an instance of java.util.Properties filled with these key value pairs.
%

%
% 2007/06/01  WHF  Created.
% 2007/06/11  WHF  Switched from cell array to instance of java.util.Properties.
%

out = java.util.Properties;
for ii = 1:optArray.length,
	kv = optArray(ii).split('=', 2);
	if kv.length == 2,
		out.put(kv(1).toLowerCase, kv(2));
	end
end
