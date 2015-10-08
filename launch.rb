#!/usr/bin/env ruby
require 'date'
require 'optparse'
require 'fileutils'

PIDFILE = "airbnb.pid"
JARFILE = "MDP-Airbnb-1.0.0.jar"

class Launcher

	@@today = Date.today.strftime("%Y-%m-%d")

	def initialize(args=ARGV)
		@args = args

		# Acción por defecto: iniciar el crawler
		@action = :start

		@out, @err, @split_files = false

		exit if parse_options == nil

		if @out and @err
			if @split_files
				@out, @err = @@today + ".log", @@today + ".err"
			else
				@out, @err = @@today + ".log"
			end
		else
			puts "[WARNING] Ignoring '--split-files'." if @split_files
			@out = @@today + ".log" if @out
			@err = @@today + ".err" if @err
		end
	end

	def launch
		case @action
		when :start
			start_crawler
		when :kill
			kill_crawler(PIDFILE)
			FileUtils.rm PIDFILE if File.exist? PIDFILE
		else
			$stderr.puts "BUG: Unrecognized action " + @action
		end
	end

	def start_crawler
		redir = {
			:out => :out,
			:err => :err
		}

		redir[:out] = @out if @out
		redir[:err] = @err if @err

		launch_crawler(PIDFILE, redir, JARFILE)
	end

	def kill_crawler(pidfile)
		File.open(pidfile) { |f|
			pid = f.gets.to_i
			Process.kill("TERM", pid)
			# Try to append a notice to the logs
			logfile = case
								when File.exist?(@@today + ".log") then File.open(@@today + ".log", "a")
								when File.exist?(@@today + ".err") then File.open(@@today + ".err", "a")
								else nil
								end
			unless logfile.nil?
				logfile.puts "[LAUNCHER] Crawler was killed (pid #{pid})"
				logfile.close
			end
		} if File.exists? pidfile
	end

	private

	def parse_options
		opt = OptionParser.new("Usage: #{$0} [options]")
			
		opt.on("--kill", "Stop the crawler") {
			@action = :kill
		}

		opt.on("-o", "--out", "Dump stdout to file") {
			@out = true
		}

		opt.on("-e", "--err", "Dump stderr to file") {
			@err = true
		}

		opt.on("-s", "--split-files", "Dump stdout and stderr in separate files") {
			@split_files = true
		}

		opt.on("-h", "--help", "Print this help") {
			puts opt
			exit
		}

		begin
			opt.parse! @args
		rescue OptionParser::InvalidOption
			puts opt
			return nil
		end
	end

	def launch_crawler(pidfile, redir, path)
		path = File.absolute_path(path)

		puts "Executing '#{path}'"
		puts "Crawler args: " + @args.join(' ') unless @args.empty?

    if @args.empty?
  		pid = spawn("java", "-jar", path, redir)
    else
      pid = spawn("java", "-jar", path, *@args, redir)
    end
		File.open(pidfile, "w") { |f| f.print pid }
		Process.wait(pid)
		puts "[LAUNCHER] Done"
		FileUtils.rm pidfile if File.exist? pidfile
	end

end

launcher = Launcher.new
launcher.launch
