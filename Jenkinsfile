@Library('socrata-pipeline-library@10.2.1') _

commonPipeline(
    jobName: 'region-coder',
    defaultBuildWorker: 'worker-java-multi-pg13',
    projects: [
        [
            name: 'region-coder',
            compiled: true,
            deploymentEcosystem: 'ecs',
            docker: [
                buildContext: 'docker',
            ],
            language: 'scala',
            type: 'service'
        ]
    ],
    teamsChannelWebhookId: 'WORKFLOW_EGRESS_AUTOMATION',
)
