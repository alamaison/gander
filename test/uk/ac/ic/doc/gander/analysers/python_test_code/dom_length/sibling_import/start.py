import sister
import brother as adam
from brother import bar

def fun():
	a = y
	a.b() # length 3
	sister.foo(a)
	a.c() # length 3
	
def fun2():
	a = y
	a.s() # length 4
	adam.bar(a, a)

def fun3():
	a = y
	a.q() # length 4
	bar(a, a)
