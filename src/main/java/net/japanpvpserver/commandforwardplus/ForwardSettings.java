package net.japanpvpserver.commandforwardplus;

import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;

import java.util.List;
import java.util.stream.Collectors;

@YamlFile
public class ForwardSettings {

    @YamlKey("ignoreCommands")
    private List<String> ignoreCommands = List.of(
            "end"
    );

    public List<String> getIgnoreCommands() {
        return ignoreCommands.stream().map(String::toLowerCase).collect(Collectors.toList());
    }
}
