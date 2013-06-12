# A variable that refers to the local binding (subtly bound in the for-loop
# variable) despite the same name being in the global namespace

# from [PEP 227]
i = 6
def f(x):
	def g():
		print i # who_am_i

	for i in x:  # ah, i *is* local to f, so this is what g sees
		pass
	g()
