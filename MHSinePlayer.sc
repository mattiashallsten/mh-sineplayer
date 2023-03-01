MHSinePlayerSynth {
	var <ratio, out, index, parent;
	var player, <is_playing;
	var mods;
	var amp = 0.2;
	var <finetune = 0.0;
	var additional_args;

	*new {| ratio = 1, out = 0, index, parent |
		^super.newCopyArgs(ratio, out, index, parent).init;
	}

	init {
		// TODO: Think of how mods are represented in the
		// `MHSinePlayerSynth' compared to `MHSinePlayer'.
		mods = 0.0!3;
		is_playing = false;
		additional_args = Dictionary.new();
	}

	play {| new_ratio, atk, rel |
		if((new_ratio.asString != ratio.asString) || (is_playing == false), {
			var arguments = Dictionary.new();

			ratio = new_ratio;
			if(is_playing, { this.stop() });

			// TODO: Setting arguments via dictionary is not
			// working properly. Maybe to slow?
			// arguments = arguments ++ Dictionary.newFrom([
			// 	\freq, parent.root * ratio.asNum * this.convert_finetune(finetune),
			// 	\out, if(parent.split_out, {parent.out + index}, {parent.out}),
			// 	\amp, amp,
			// 	\atk, atk ? parent.atk,
			// 	\rel, rel ? parent.rel,
			// 	\mod1, mods[0],
			// 	\mod2, mods[1],
			// 	\mod3, mods[2]
			// ]);
			
			// arguments = arguments ++ additional_args;
			
			// arguments = arguments.getPairs;

			// if(parent.debug, {
			// 	"In `MHSinePlayerSynth.play': playing sine with arguments: %".format(arguments).postln;
			// });
			
			player = Synth(parent.synthdef, [
				\freq, parent.root * ratio.asNum * this.convert_finetune(finetune),
				\out, if(parent.split_out, {parent.out + index}, {parent.out}),
				\amp, amp,
				\atk, atk ? parent.atk,
				\rel, rel ? parent.rel,
				\mod1, mods[0],
				\mod2, mods[1],
				\mod3, mods[2]
			] ++ additional_args.getPairs, parent.target);

			if(parent.debug, {
				"In `MHSinePlayerSynth.play': playing synth with additional arguments: %".format(additional_args).postln;
			});

			if(parent.debug, {
				"In `MHSinePlayerSynth.play': created synth %".format(player).postln;
			});
			
			is_playing = true;
			// FIXME: Quickfix for bug where the frequency is not
			// properly set using the dictionary.
			
			this.update_param();
		});
	}

	update_freq {
		if(is_playing, {
			player.set(\freq, parent.root * this.ratio.asNum * this.convert_finetune(this.finetune))
		})
	}

	glide {| new_ratio, time = 1 |
		if(is_playing, {
			player.set(\glide_time, time);
			ratio = new_ratio ? ratio;
			player.set(\freq, parent.root * ratio.asNum);
		}, {
			this.play(new_ratio);
		});
		this.update_param();
	}

	stop {
		if(is_playing, {
			player.set(\gate, 0);
			player = nil;
			is_playing = false;
		});
	}

	set_mod {|mod_index = 0.0, val|
		mods.clipPut(mod_index, val);
		this.update_param();
	}

	set_amp {|val|
		amp = val;
		this.update_param();
	}

	update_param {
		if(is_playing, {
			mods.do{|val, i|
				var param = ("mod" ++ (i + 1).asString).asSymbol;
				player.set(param, val);
			};
			player.set(\freq, parent.root * ratio.asNum * this.convert_finetune(finetune));
			player.set(\amp, amp);
			player.set(additional_args.getPairs);
		})
	}

	set_finetune {|val|
		finetune = val;
		this.update_param();
	}

	convert_finetune {|ft|
		^ft.midiratio;
	}

	get_freq {
		if(is_playing, {
			^parent.root * ratio.asNum * this.convert_finetune(finetune);
		}, {
			nil
		});
	}

	set_value {|key, val|
		additional_args[key] = val;

		if(is_playing, {
			if(parent.debug, {
				"In `MHSinePlayerSynth.set_value': setting key '%' to value %".format(key, val).postln;
			});
			
			player.set(key, val)
		})
	}
}

MHSinePlayerStep {
	var <ratio, <glide, <atk, <rel, <mod1, <mod2, info, <function, <num, <additional_param;

	*new {| step, parent |
		^super.new.init(step, parent);
	}

	init {| step, parent |
		ratio      = nil!parent.num_voices;
		glide      = nil!parent.num_voices;
		atk        = parent.atk!parent.num_voices;
		rel        = parent.rel!parent.num_voices;
		mod1       = nil!parent.num_voices;
		mod2       = nil!parent.num_voices;
		info       = "";
		num        = nil;
		function   = nil;
		additional_param = ();

		// Check ratio
		if(step[\ratio].notNil, {
			step[\ratio].do{|r, i|
				ratio[i] = r;
			};
		});

		// Check glide
		if(step[\glide].notNil, {
			if(step[\glide].isArray, {
				step[\glide].do{|g, i|
					glide[i] = g;
				}
			}, {
				glide = step[\glide]!parent.num_voices;
			})
		});

		// Check atk
		if(step[\atk].notNil, {
			if(step[\atk].isArray, {
				step[\atk].do{|g, i|
					atk[i] = g;
				}
			}, {
				atk = step[\atk]!parent.num_voices;
			})
		});

		// Check rel
		if(step[\rel].notNil, {
			if(step[\rel].isArray, {
				step[\rel].do{|g, i|
					rel[i] = g;
				}
			}, {
				rel = step[\rel]!parent.num_voices;
			})
		});

		// Check mod1
		if(step[\mod1].notNil, {
			if(step[\mod1].isArray, {
				step[\mod1].do{|g, i|
					mod1[i] = g;
				}
			}, {
				mod1 = step[\mod1]!parent.num_voices;
			})
		});

		// Check mod2
		if(step[\mod2].notNil, {
			if(step[\mod2].isArray, {
				step[\mod2].do{|g, i|
					mod2[i] = g;
				}
			}, {
				mod2 = step[\mod2]!parent.num_voices;
			})
		});

		if(step[\info].notNil, {
			info = step[\info]
		});

		// Check function
		if(step[\function].notNil, {
			function = step[\function]
		});

		// Check num
		if(step[\num].notNil, {
			num = step[\num]
		});

		step.pairsDo{|key, val|
			if((key != 'ratio') &&
				(key != 'glide') &&
				(key != 'atk') &&
				(key != 'rel') &&
				(key != 'mod1') &&
				(key != 'mod2') &&
				(key != 'info') &&
				(key != 'function'), {
					additional_param[key] = val;
				})
		};
	}

	has_function { ^function.notNil }

	get_info { ^info }

	get_ratios {
		var str = "";
		ratio.do{|r|
			if(r.notNil, {
				str = str ++ r.asString ++ ", ";
			});
		};
		^str;
	}
}

MHSinePlayer {
	var <server, <root, <num_voices, <out, <split_out, <synthdef, <target, <debug = false;
	var <atk = 0.2, <rel = 1;
	var <sines;
	var seq, <curr_index = nil, <next_index = 0;
	var stop_func;
	var playing = false;

	var <atk = 2, <rel = 2;

	*new {| server, root = 200, num_voices = 4, out = 0, split_out = false, synthdef, target = 0, debug = false |
		^super.newCopyArgs(server, root, num_voices, out, split_out, synthdef, target, debug).init;
	}

	init {
		"======== MHSinePlayer ========".postln;
		server = server ? Server.default;
		synthdef = synthdef ? \mh_sine_player_sines;
		"Using synthdef %".format(synthdef).postln;

		forkIfNeeded {
			this.load_synth_defs;
			server.sync;
			sines = this.load_sines(num_voices);
		}
	}

	set_num_voices {|n|
		num_voices = n;
		sines = this.load_sines(num_voices);
	}

	set_sequence {| sequence |
		// Sequence should be a list of events (however, not used as
		// ordinary events in SuperCollider), containing the following
		// keys:
		//
		// - ratio :: a list of `Ratio' objects or floats, the same
		//            length as `num_sines'. If an element is `nil' it
		//            means that that voice should not play.
		//
		// (optional)
		//
		// - glide :: a list of floats, the same length as
		//            `num_sines'. If an element is not nil it means
		//            that that voice should glide to the specified
		//            ratio with the duration of the float. If it is
		//            `nil', it means no glide. If the `glide' key
		//            does not exist, none of the voices glide.
		//
		// - function :: a function to be run when the step is
		//               triggered. takes two arguments: the current
		//               index and this MHSinePlayer

		if(debug, { "In 'MHSinePlayer.set_sequence': setting sequence...".postln; });

		seq = [];
		seq = sequence.collect{|step| MHSinePlayerStep.new(step, this.value()) };
	}

	set_index {|new_index|
		next_index = new_index.clip(0, seq.size - 1);
		if(debug, { "In `MHSinePlayer.set_index': setting `next_index' to %".format(next_index).postln; });
	}

	set_stopfunc {|func|
		stop_func = func;
	}

	set_root {|val|
		root = val;
		sines.do{|sin| sin.update_freq(); };
	}

	play {| advance = true |
		if(seq.notNil, {
			//var glide = false;
			var step;
			curr_index = next_index.clip(0, seq.size - 1);
			if(debug, { "In 'MHSinePlayer.play': `seq' was not nil, setting `curr_index' to `next_index' (%)".format(curr_index).postln; });

			step = seq[curr_index];

			sines.do{|sine, i|
				if(step.ratio[i].notNil, {
					var time = step.glide[i];
					if(time.notNil, {
						sine.glide(step.ratio[i], time)
					}, {
						sine.play(step.ratio[i], step.atk[i], step.rel[i])
					})
				}, {
					sine.stop();
				});
			};

			if(step.has_function(), {
				step.function.value(curr_index, this.value());
			});

			step.additional_param.pairsDo{|key, val|
				sines.do{|sine, i|
					sine.set_value(key, val.asArray.clipAt(i))
				}
			};

			if(advance, {
				next_index = curr_index + 1;
				if(next_index >= seq.size, {
					if(debug, { "In 'MHSinePlayer.play': can't advance `next_index', already at last step %.".format(curr_index).postln });
					next_index = next_index.clip(0, seq.size - 1);
				}, {
					if(debug, { "In 'MHSinePlayer.play': advanced to next step %.".format(next_index).postln; });
				});
			});
			playing = true;
			^curr_index;
		});
	}

	stop {
		sines.do{|sine| sine.stop };
		if(stop_func.notNil, {
			stop_func.value(this.value);
		});
		playing = false;
		^curr_index;
	}

	reset {
		next_index = 0;
		curr_index = nil;
		this.stop();
	}

	get_current_ratios {
		if(seq.notNil && playing, {
			var step = seq.clipAt(curr_index);
			^step[\ratio]
		}, {
			[]
		});
	}

	get_freqs {
		var freqs = sines.collect{|sine|
			sine.get_freq()
		};
		^freqs;
	}

	get_info {|index = 0|
		if(seq.notNil, {
			^seq[index].get_info()
		});
	}

	get_ratios {|index = 0|
		if(seq.notNil, {
			^seq[index].get_ratios()
		})
	}

	get_current_ratios2 {
		// HACKY QUICK FIX
		if((seq.notNil) && (curr_index.notNil) , {
			var step = seq.clipAt(curr_index);
			^step.ratio;
		}, {
			^nil!num_voices;
		});
	}

	get_step {|index|
		index = index ? curr_index;
		if((index.notNil) && (seq.notNil), {
			^seq[index];
		}, {
			^nil
		})
	}

	get_size {
		^seq.size;
	}

	set_mod {|synth_index = "all", mod_index = 0, val = 0.0|
		if(synth_index == "all", {
			sines.do{|sine|
				sine.set_mod(mod_index, val)
			}
		}, {
			sines.clipAt(synth_index).set_mod(mod_index, val);
		});
	}

	set_amp {|synth_index = "all", val|
		if(synth_index == "all", {
			sines.do{|sine|
				sine.set_amp(val);
			}
		}, {
			sines.clipAt(synth_index).set_amp(val);
		});
	}

	set_finetune {|synth_index = "all", val|
		if(synth_index == "all", {
			sines.do{|sine|
				sine.set_finetune(val)
			}
		}, {
			sines.clipAt(synth_index).set_finetune(val)
		});
	}

	set_value {|synth_index = "all", key, val|
		if(debug, {
			"In `MHSinePlayer.set_value': setting sine index % key '%' to value %".format(synth_index, key, val).postln;
		});

		if(synth_index == "all", {
			sines.do{|sine|
				sine.set_value(key, val)
			}
		}, {
			sines.clipAt(synth_index).set_value(key, val)
		});
	}

	load_synth_defs {
		SynthDef(\mh_sine_player_sines, {
			| freq = 440
			, glide_time = 0.2
			, out = 0
			, gate = 1
			, amp = 0.2
			, atk = 2
			, rel = 2
			, mod1 = 0.0
			, mod2 = 0.0
			, mod3 = 0.0
			|

			var env = EnvGen.kr(Env.asr(atk, 1, rel), gate, doneAction:2);
			var sig = SinOsc.ar(freq.lag(glide_time)) * env * amp;

			Out.ar(out, sig);
		}).add;
	}

	load_sines {| num_voices |
		^num_voices.collect{|i|
			MHSinePlayerSynth.new(Ratio(1,1), out, i, this.value())
		}
	}
}