
function DT = DTstruct()

% DT = DTstruct()
%   Return template DT structure:
%       DT.server = 'localhost';
%       DT.source = '_Metrics';
%       DT.start = 0;
%       DT.duration = 0;
%       DT.reference = 'newest';
%       DT.chan = '*';

DT.server = 'localhost';
DT.source = '_Metrics';
DT.start = 0;
DT.duration = 0;
DT.reference = 'newest';
DT.chan = '*';

end
