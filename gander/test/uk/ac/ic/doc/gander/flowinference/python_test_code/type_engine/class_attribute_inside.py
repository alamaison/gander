class A:
	i = "Hello"
	j = 42
	
	def f(self):
		i = 99
		print A.i # what_am_i
	
	def g(self):
		i = 99
		print self.i # what_am_i_via_self
