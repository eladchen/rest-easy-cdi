package com.example.boot;

class NamedBootService implements NamedObject<BootService> {
    private final String name;
    private final BootService bootService;

    public NamedBootService( BootService bootService, String name ) {
        if ( name == null || name.isEmpty() ) {
            throw new IllegalArgumentException( "name can not be null" );
        }

        if ( bootService == null ) {
            throw new IllegalArgumentException( "bootService can not be null" );
        }

        this.name = name;
        this.bootService = bootService;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public BootService getNamedObject() {
        return bootService;
    }
}
