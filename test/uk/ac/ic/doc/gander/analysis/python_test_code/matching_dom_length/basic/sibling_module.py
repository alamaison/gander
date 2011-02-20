def call_me_my_brother(x):
	x.f()
	if x.g():
		x.h()
	local_call(x)

def local_call(z):
	z.billy()
	
import stepbrother

def call_me_my_brother_deep(arg):
	arg.f()
	if arg.g():
		arg.h()
	stepbrother.i_only_get_called_via_sibling(arg)
