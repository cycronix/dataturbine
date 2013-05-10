function [ DTgot ] = DTfetch( DT )
%DTgot = DTfetch(DT)
%   Wrapper to fetch array of DT structures.  See DTget()

for i=1:length(DT)
    DTgot(i) = DTget(DT(i));
end

end

