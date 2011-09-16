# from [PEP 227]
i = 6
def f(x):
	def g():
		print i # who_am_i

	for i in x:  # ah, i *is* local to f, so this is what g sees
		pass
	g()
