function y=p_alt(x)
%disp('lin called');

if nargin==0
    y=3;
else
    y=x{1} + x{2}*x{3};
end
