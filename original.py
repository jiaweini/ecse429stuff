import sys

s=sys.argv[1]
num1 = int(s)
num2 = 3
#3 dollar per can of soda
num3 =2;
#200% tax
x=num1-(num2*num3)
if x == 0:
    # indented four spaces
    print("enjoy");
elif x>0 :
	print("take the change");
else:
    print("not enough money");
