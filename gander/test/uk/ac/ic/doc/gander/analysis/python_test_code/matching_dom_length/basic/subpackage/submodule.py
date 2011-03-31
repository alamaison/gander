def call_me_auntie(boo):
	boo.show()
	another_local_call(boo)

def another_local_call(goat):
	goat.baaa()

import sibling_module

def call_me_auntie_and_see_how_clever_i_am(baz):
	baz.gurgle()
	sibling_module.call_me_my_brother_deep(baz)
	